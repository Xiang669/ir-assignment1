
#!/usr/bin/env bash

set -euo pipefail

# ==========================================
# 1. Paths
# ==========================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

RESOURCES="$PROJECT_ROOT/src/main/resources"

QRELS="$RESOURCES/QRelsCorrectedforTRECeval"
RUN_DIR="$RESOURCES/output"
RESULT_DIR="$PROJECT_ROOT/evaluation_results"

# trec_eval executable path
TREC_EVAL="${1:-trec_eval}"

mkdir -p "$RESULT_DIR"

SUMMARY="$RESULT_DIR/summary.csv"

# ==========================================
# 2. Check files
# ==========================================

if ! command -v "$TREC_EVAL" >/dev/null 2>&1; then
    echo "ERROR: trec_eval not found: $TREC_EVAL"
    exit 1
fi

if [[ ! -f "$QRELS" ]]; then
    echo "ERROR: Qrels file not found: $QRELS"
    exit 1
fi

if [[ ! -d "$RUN_DIR" ]]; then
    echo "ERROR: Run directory not found: $RUN_DIR"
    exit 1
fi

# Check qrels format: query_id 0 doc_id relevance
if ! awk '
    NF > 0 && NF != 4 {
        print "Invalid qrels line:", NR, $0 > "/dev/stderr"
        bad = 1
    }
    NF > 0 { count++ }
    END { exit (bad || count == 0) }
' "$QRELS"; then
    echo "ERROR: Invalid qrels format"
    exit 1
fi

# ==========================================
# 3. Initialize CSV
# ==========================================

echo "Run,MAP,P10,nDCG10,Recall100" > "$SUMMARY"

count=0

# Avoid errors if output directory is empty
shopt -s nullglob

# ==========================================
# 4. Evaluate all run files
# ==========================================

for RUN_FILE in "$RUN_DIR"/*; do

    [[ -f "$RUN_FILE" ]] || continue

    # Validate TREC six-column format
    if ! awk '
        NF > 0 {
            count++
            if (NF != 6 || $2 != "Q0" ||
                $4 !~ /^[0-9]+$/) {
                bad = 1
            }
        }
        END { exit (bad || count == 0) }
    ' "$RUN_FILE"; then

        echo "Skipping invalid file: $RUN_FILE"
        continue
    fi

    RUN_NAME="$(basename "$RUN_FILE")"
    RESULT_FILE="$RESULT_DIR/${RUN_NAME}.eval.txt"

    echo "=================================="
    echo "Evaluating: $RUN_NAME"
    echo "=================================="

    # Evaluate aggregate results only (no -q)
    "$TREC_EVAL" \
        -c -l 1 -M1000 \
        -m map \
        -m P.10 \
        -m ndcg_cut.10 \
        -m recall.100 \
        "$QRELS" \
        "$RUN_FILE" > "$RESULT_FILE"

    # Extract all-query aggregate metrics
    awk -v run="$RUN_NAME" '

        $1 == "map" && $2 == "all" {
            map = $3
        }

        $1 == "P_10" && $2 == "all" {
            p10 = $3
        }

        $1 == "ndcg_cut_10" && $2 == "all" {
            ndcg = $3
        }

        $1 == "recall_100" && $2 == "all" {
            recall = $3
        }

        END {
            if (map == "" || p10 == "" ||
                ndcg == "" || recall == "") {

                print "ERROR: Missing metrics for " run \
                    > "/dev/stderr"
                exit 1
            }

            printf "\"%s\",%s,%s,%s,%s\n",
                run, map, p10, ndcg, recall
        }

    ' "$RESULT_FILE" >> "$SUMMARY"

    cat "$RESULT_FILE"
    echo

    count=$((count + 1))

done

# ==========================================
# 5. Summary
# ==========================================

echo "=================================="
echo "Evaluation completed!"
echo "=================================="

echo "Total evaluated runs: $count"
echo "Summary saved to: $SUMMARY"

if [[ "$count" -ne 16 ]]; then
    echo "WARNING: Expected 16 runs, found $count"
fi

if [[ "$count" -eq 0 ]]; then
    echo "ERROR: No valid run files found"
    exit 1
fi
