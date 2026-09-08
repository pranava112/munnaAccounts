# 1. First: Don't make AI calculate everything

# This is very important for your architecture.

# You should divide your accounting engine into:

#                  ACCOUNTING AI ENGINE
#                          │
#           ┌──────────────┴──────────────┐
#           │                             │
#      RULE ENGINE                    AI ENGINE
#           │                             │
#    Exact calculations            Understanding
#    Ledger posting                Prediction
#    GST calculation               Classification
#    Stock calculation             Anomaly detection
#    Trial balance                 Suggestions
#    P&L                           Natural language
#    Balance sheet                 Document extraction
# Rule engine should handle
# Debit/Credit
# Ledger balances
# Trial Balance
# Stock quantity
# Stock valuation
# Gross Profit
# Net Profit
# Balance Sheet
# GST calculations
# Invoice totals
# Outstanding balances
# Capital
# Drawings

# These must be deterministic, not AI-generated.

# AI should handle
# Understanding user text
# Automatic account selection
# Automatic voucher type
# Document/PDF extraction
# Transaction classification
# Error detection
# Fraud/anomaly detection
# Business predictions
# Cash-flow forecasting
# Sales forecasting
# Expense forecasting
# Customer payment prediction
# Supplier payment prediction
# Business recommendations
# Natural-language accounting assistant

