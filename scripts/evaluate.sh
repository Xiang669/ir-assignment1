
#!/usr/bin/env bash

set -euo pipefail

# ==========================================
# 1. Path Configuration
# ==========================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

RESOURCES="$PROJECT_ROOT/src/main/resources"

QRELS="$RESOURCES/QRelsCorrectedforTRECeval"
RUN_DIR="$RESOURCES/output"
RESULT_DIR="$PROJECT_ROOT/evaluation_results"

# Use provided trec_eval path or system command
TREC_EVAL="${1:-trec_eval}"

TOP_K=50
SUMMARY="$RESULT_DIR/summary.csv"

mkdir -p "$RESULT_DIR"

# ==========================================
# 2. Validate Input Files
# ==========================================

if ! command -v "$TREC_EVAL" >/dev/null 2>&1; then
    echo "ERROR: trec_eval not found: $TREC_EVAL" >&2
    exit 1
fi

if [[ ! -f "$QRELS" ]]; then
    echo "ERROR: Qrels file not found: $QRELS" >&2
    exit 1
fi

if [[ ! -d "$RUN_DIR" ]]; then
    echo "ERROR: Run directory not found: $RUN_DIR" >&2
    exit 1
fi

# Validate four-column TREC qrels
if ! awk '
    NF > 0 {
        rows++
        if (NF != 4) bad = 1
    }
    END {
        exit (bad || rows == 0)
    }
' "$QRELS"; then
    echo "ERROR: Invalid qrels format" >&2
    exit 1
fi

# ==========================================
# 3. Initialize CSV
# ==========================================

echo "Run,MAP,P@5,P@10,Rprec,nDCG@10,Recall@50" > "$SUMMARY"

count=0

shopt -s nullglob

# ==========================================
# 4. Evaluate All Experiments
# ==========================================

for RUN_FILE in "$RUN_DIR"/*; do

    [[ -f "$RUN_FILE" ]] || continue

    FILENAME="$(basename "$RUN_FILE")"
    RUN_NAME="${FILENAME%.*}"

    # Validate six-column TREC format and Top 50
    if ! awk -v limit="$TOP_K" '
        NF > 0 {
            rows++

            if (NF != 6 ||
                $2 != "Q0" ||
                $4 !~ /^[0-9]+$/ ||
                $4 < 1 ||
                $4 > limit) {
                bad = 1
            }

            count[$1]++

            if (count[$1] > limit) {
                bad = 1
            }
        }

        END {
            exit (bad || rows == 0)
        }
    ' "$RUN_FILE"; then

        echo "Skipping invalid run file: $FILENAME"
        continue
    fi

    echo "======================================"
    echo "Evaluating: $RUN_NAME"
    echo "======================================"

    # ======================================
    # Run trec_eval
    # ======================================

    RESULT=$("$TREC_EVAL" \
        -c -l 1 -M50 \
        -m map \
        -m P.5,10 \
        -m Rprec \
        -m ndcg_cut.10 \
        -m recall.50 \
        "$QRELS" \
        "$RUN_FILE")

    # Print results directly to terminal
    printf '%s\n' "$RESULT"
    echo ""

    # ======================================
    # Extract Aggregate Results to CSV
    # ======================================

    printf '%s\n' "$RESULT" | awk -v run="$RUN_NAME" '

        $2 == "all" {
            values[$1] = $3
        }

        END {
            n = split("map P_5 P_10 Rprec ndcg_cut_10 recall_50", keys, " ")

            for (i = 1; i <= n; i++) {
                if (!(keys[i] in values)) {
                    print "ERROR: Missing metric " keys[i] > "/dev/stderr"
                    exit 1
                }
            }

            printf "\"%s\",%s,%s,%s,%s,%s,%s\n", \
                run, \
                values["map"], \
                values["P_5"], \
                values["P_10"], \
                values["Rprec"], \
                values["ndcg_cut_10"], \
                values["recall_50"]
        }

    ' >> "$SUMMARY"

    count=$((count + 1))

done

# ==========================================
# 5. Final Summary
# ==========================================

echo "======================================"
echo "Evaluation Completed"
echo "======================================"

echo "Total evaluated runs: $count"
echo "CSV saved to: $SUMMARY"

if [[ "$count" -eq 0 ]]; then
    echo "ERROR: No valid run files found" >&2
    exit 1
fi

if [[ "$count" -ne 16 ]]; then
    echo "WARNING: Expected 16 runs, found $count"
fi
