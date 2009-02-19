package it.geosolutions.iengine.flow.event.consumer;

public enum EventConsumerStatus {
    /**
     * Idle EventConsumerStatus CODE
     */
    IDLE,

    /**
     * Waiting EventConsumerStatus CODE
     */
    WAITING,

    /**
     * Processing EventConsumerStatus CODE
     */
    EXECUTING,

    /**
     * Finished OK EventConsumerStatus CODE
     */
    COMPLETED,

    /**
     * Finished KO EventConsumerStatus CODE
     */
    FAILED;

}
