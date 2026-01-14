#!/bin/bash
# Firebase Credential Cleanup Script
# ===================================
# This script removes sensitive Firebase/Google Cloud configuration files
# from the git history using BFG Repo-Cleaner.
#
# PREREQUISITES:
# 1. Install BFG: brew install bfg
# 2. Have a backup of your repository
# 3. Ensure all collaborators are notified before force-pushing
#
# USAGE: ./cleanup-credentials.sh

set -e

echo "=========================================="
echo "Firebase Credential Cleanup Script"
echo "=========================================="
echo ""

# Check if BFG is installed
if ! command -v bfg &> /dev/null; then
    echo "ERROR: BFG Repo-Cleaner is not installed."
    echo "Install with: brew install bfg"
    exit 1
fi

# Confirm before proceeding
echo "⚠️  WARNING: This script will:"
echo "  1. Remove google-services.json from ALL git history"
echo "  2. Remove GoogleService-Info.plist from ALL git history"
echo "  3. Remove firebase_options.dart from ALL git history"
echo ""
echo "This is a DESTRUCTIVE operation that rewrites git history."
echo "All collaborators will need to re-clone the repository."
echo ""
read -p "Are you sure you want to continue? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "Aborted."
    exit 0
fi

# Get the repository root
REPO_ROOT=$(git rev-parse --show-toplevel)
cd "$REPO_ROOT"

echo ""
echo "Step 1: Creating backup..."
BACKUP_DIR="../ma-backup-$(date +%Y%m%d-%H%M%S).git"
git clone --mirror . "$BACKUP_DIR"
echo "Backup created at: $BACKUP_DIR"

echo ""
echo "Step 2: Removing google-services.json from history..."
bfg --delete-files google-services.json --no-blob-protection .

echo ""
echo "Step 3: Removing GoogleService-Info.plist from history..."
bfg --delete-files GoogleService-Info.plist --no-blob-protection .

echo ""
echo "Step 4: Removing firebase_options.dart from history..."
bfg --delete-files firebase_options.dart --no-blob-protection .

echo ""
echo "Step 5: Cleaning up..."
git reflog expire --expire=now --all
git gc --prune=now --aggressive

echo ""
echo "=========================================="
echo "Cleanup complete!"
echo "=========================================="
echo ""
echo "NEXT STEPS:"
echo "1. Review the changes with: git log --oneline"
echo "2. Force push with: git push origin --force --all"
echo "3. Force push tags with: git push origin --force --tags"
echo "4. Notify all collaborators to re-clone the repository"
echo "5. After project access is restored, rotate ALL API keys"
echo ""
echo "⚠️  IMPORTANT: The credentials are still compromised!"
echo "You MUST rotate all API keys once Google restores access."
