package utils;

import java.util.HashSet;
import java.util.Set;

public class DirtyTracker {
    
    private final Set<String> dirtyFields;
    private boolean isTrackingEnabled;
    
    public DirtyTracker() {
        this.dirtyFields = new HashSet<>();
        this.isTrackingEnabled = true;
    }
    
    /**
     * Mark a field as dirty (changed)
     * @param fieldName Name of the database column that changed
     */
    public void markDirty(String fieldName) {
        if (isTrackingEnabled && fieldName != null && !fieldName.isEmpty()) {
            dirtyFields.add(fieldName);
        }
    }
    
    /**
     * Mark multiple fields as dirty
     * @param fieldNames Array of field names
     */
    public void markDirty(String... fieldNames) {
        if (isTrackingEnabled && fieldNames != null) {
            for (String field : fieldNames) {
                if (field != null && !field.isEmpty()) {
                    dirtyFields.add(field);
                }
            }
        }
    }
    
    /**
     * Check if any field is dirty
     * @return true if there are changes to save
     */
    public boolean isDirty() {
        return !dirtyFields.isEmpty();
    }
    
    /**
     * Check if a specific field is dirty
     * @param fieldName Field name to check
     * @return true if this field has been modified
     */
    public boolean isDirty(String fieldName) {
        return dirtyFields.contains(fieldName);
    }
    
    /**
     * Get all dirty fields
     * @return Set of field names that have changed
     */
    public Set<String> getDirtyFields() {
        return new HashSet<>(dirtyFields);
    }
    
    /**
     * Clear all dirty flags after save
     * Call this ONLY after successful database update
     */
    public void clear() {
        dirtyFields.clear();
    }
    
    /**
     * Clear a specific field's dirty flag
     * @param fieldName Field to clear
     */
    public void clear(String fieldName) {
        dirtyFields.remove(fieldName);
    }
    
    /**
     * Get count of dirty fields
     * @return Number of fields that need updating
     */
    public int getDirtyCount() {
        return dirtyFields.size();
    }
    
    /**
     * Temporarily disable tracking
     * Useful when loading data from database
     */
    public void disableTracking() {
        this.isTrackingEnabled = false;
    }
    
    /**
     * Re-enable tracking after loading
     */
    public void enableTracking() {
        this.isTrackingEnabled = true;
        this.dirtyFields.clear(); // Clear any flags set during load
    }
    
    /**
     * Reset tracker - clear all and enable tracking
     */
    public void reset() {
        this.dirtyFields.clear();
        this.isTrackingEnabled = true;
    }
    
    @Override
    public String toString() {
        return "DirtyTracker{" +
                "dirtyFields=" + dirtyFields +
                ", trackingEnabled=" + isTrackingEnabled +
                '}';
    }
}
