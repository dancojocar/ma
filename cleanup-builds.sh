#!/bin/bash

# Build Cleanup Script for Android, Flutter, and iOS projects
# This script removes build artifacts to free up disk space

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LECTURES_DIR="${SCRIPT_DIR}/lectures"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
ANDROID_CLEANED=0
FLUTTER_CLEANED=0
IOS_CLEANED=0
TOTAL_SIZE_FREED=0

# Dry run mode
DRY_RUN=false

print_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -d, --dry-run    Show what would be deleted without actually deleting"
    echo "  -h, --help       Show this help message"
    echo ""
    echo "This script cleans up build artifacts from Android, Flutter, and iOS projects"
    echo "in the lectures directory."
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -d|--dry-run)
            DRY_RUN=true
            shift
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            print_usage
            exit 1
            ;;
    esac
done

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_dry() {
    echo -e "${YELLOW}[DRY-RUN]${NC} Would delete: $1"
}

get_dir_size() {
    if [[ -d "$1" ]]; then
        du -sk "$1" 2>/dev/null | cut -f1
    else
        echo 0
    fi
}

format_size() {
    local size_kb=$1
    if [[ -z "$size_kb" ]] || [[ "$size_kb" -eq 0 ]]; then
        echo "0KB"
    elif [[ $size_kb -ge 1048576 ]]; then
        local gb=$((size_kb / 1048576))
        local remainder=$((size_kb % 1048576 * 100 / 1048576))
        echo "${gb}.${remainder}GB"
    elif [[ $size_kb -ge 1024 ]]; then
        local mb=$((size_kb / 1024))
        local remainder=$((size_kb % 1024 * 100 / 1024))
        echo "${mb}.${remainder}MB"
    else
        echo "${size_kb}KB"
    fi
}

# Returns 0 if directory was removed, 1 otherwise
remove_dir() {
    local dir="$1"
    if [[ -d "$dir" ]]; then
        local size=$(get_dir_size "$dir")
        if [[ "$DRY_RUN" == true ]]; then
            log_dry "$dir ($(format_size $size))"
        else
            rm -rf "$dir"
            log_success "Removed $dir ($(format_size $size))"
        fi
        TOTAL_SIZE_FREED=$((TOTAL_SIZE_FREED + size))
        echo "removed"
        return 0
    fi
    return 0
}

# Clean Android project
clean_android() {
    local project_dir="$1"
    local cleaned=""
    
    # Main build directory
    cleaned+=$(remove_dir "${project_dir}/build")
    
    # .gradle cache directory
    cleaned+=$(remove_dir "${project_dir}/.gradle")
    
    # .kotlin cache directory
    cleaned+=$(remove_dir "${project_dir}/.kotlin")
    
    # App module build directory
    cleaned+=$(remove_dir "${project_dir}/app/build")
    
    # Other module build directories
    for module_dir in "${project_dir}"/*/; do
        if [[ -d "${module_dir}build" ]] && [[ "$(basename "$module_dir")" != "app" ]]; then
            cleaned+=$(remove_dir "${module_dir}build")
        fi
    done
    
    if [[ -n "$cleaned" ]]; then
        ((ANDROID_CLEANED++)) || true
    fi
}

# Clean Flutter project
clean_flutter() {
    local project_dir="$1"
    local cleaned=""
    
    # Main build directory
    cleaned+=$(remove_dir "${project_dir}/build")
    
    # .dart_tool directory
    cleaned+=$(remove_dir "${project_dir}/.dart_tool")
    
    # Android build within Flutter project
    cleaned+=$(remove_dir "${project_dir}/android/.gradle")
    cleaned+=$(remove_dir "${project_dir}/android/app/build")
    
    # iOS build within Flutter project
    cleaned+=$(remove_dir "${project_dir}/ios/.symlinks")
    cleaned+=$(remove_dir "${project_dir}/ios/Pods")
    
    # macOS build within Flutter project
    cleaned+=$(remove_dir "${project_dir}/macos/.symlinks")
    cleaned+=$(remove_dir "${project_dir}/macos/Pods")
    
    # Web build
    cleaned+=$(remove_dir "${project_dir}/web/build")
    
    # Remove ephemeral files in each platform
    for platform in ios macos linux windows; do
        cleaned+=$(remove_dir "${project_dir}/${platform}/Flutter/ephemeral")
    done
    
    if [[ -n "$cleaned" ]]; then
        ((FLUTTER_CLEANED++)) || true
    fi
}

# Clean iOS/macOS project (standalone, not part of Flutter)
clean_ios() {
    local project_dir="$1"
    local cleaned=""
    
    # DerivedData (usually outside project, but check locally)
    cleaned+=$(remove_dir "${project_dir}/DerivedData")
    
    # Build directory
    cleaned+=$(remove_dir "${project_dir}/build")
    
    # Pods directory
    cleaned+=$(remove_dir "${project_dir}/Pods")
    
    # xcuserdata - find all instances
    while IFS= read -r -d '' xcuser_dir; do
        cleaned+=$(remove_dir "$xcuser_dir")
    done < <(find "$project_dir" -name "xcuserdata" -type d -print0 2>/dev/null)
    
    if [[ -n "$cleaned" ]]; then
        ((IOS_CLEANED++)) || true
    fi
}

# Detect project type and clean accordingly
detect_and_clean() {
    local project_dir="$1"
    
    # Skip if not a directory
    [[ ! -d "$project_dir" ]] && return
    
    # Check for Flutter project (has pubspec.yaml)
    if [[ -f "${project_dir}/pubspec.yaml" ]]; then
        log_info "Flutter project: ${project_dir#$SCRIPT_DIR/}"
        clean_flutter "$project_dir"
        return
    fi
    
    # Check for Android project (has build.gradle or build.gradle.kts)
    if [[ -f "${project_dir}/build.gradle" ]] || [[ -f "${project_dir}/build.gradle.kts" ]]; then
        log_info "Android project: ${project_dir#$SCRIPT_DIR/}"
        clean_android "$project_dir"
        return
    fi
    
    # Check for iOS/macOS project (has .xcodeproj or .xcworkspace)
    if compgen -G "${project_dir}/*.xcodeproj" > /dev/null 2>&1 || compgen -G "${project_dir}/*.xcworkspace" > /dev/null 2>&1; then
        log_info "iOS/macOS project: ${project_dir#$SCRIPT_DIR/}"
        clean_ios "$project_dir"
        return
    fi
}

echo "========================================"
echo "  Build Cleanup Script"
echo "========================================"
if [[ "$DRY_RUN" == true ]]; then
    echo -e "${YELLOW}Running in DRY-RUN mode - no files will be deleted${NC}"
fi
echo ""

# Find all potential project directories
log_info "Scanning for projects in ${LECTURES_DIR}..."
echo ""

# Walk through lecture directories
for lecture_dir in "${LECTURES_DIR}"/*/; do
    if [[ -d "$lecture_dir" ]]; then
        for project_dir in "${lecture_dir}"/*/; do
            if [[ -d "$project_dir" ]]; then
                detect_and_clean "$project_dir"
            fi
        done
        
        # Also check the lecture directory itself (in case it's a project)
        detect_and_clean "$lecture_dir"
    fi
done

echo ""
echo "========================================"
echo "  Cleanup Summary"
echo "========================================"
echo "Android projects cleaned: $ANDROID_CLEANED"
echo "Flutter projects cleaned: $FLUTTER_CLEANED"
echo "iOS/macOS projects cleaned: $IOS_CLEANED"
echo ""
if [[ "$DRY_RUN" == true ]]; then
    echo -e "Space that would be freed: ${GREEN}$(format_size $TOTAL_SIZE_FREED)${NC}"
else
    echo -e "Total space freed: ${GREEN}$(format_size $TOTAL_SIZE_FREED)${NC}"
fi
echo "========================================"
