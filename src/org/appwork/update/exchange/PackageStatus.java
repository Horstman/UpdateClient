package org.appwork.update.exchange;

public enum PackageStatus {
    /**
     * Zip Generation is in progress
     */
    IN_PROGRESS,
    /**
     * Unknown so far
     */
    UNKNOWN,
    /**
     * This zip already exists
     */
    READY, FAILED
}