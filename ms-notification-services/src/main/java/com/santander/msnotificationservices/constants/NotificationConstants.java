package com.santander.msnotificationservices.constants;

public class NotificationConstants {
    private NotificationConstants() {}

    public static final String RABBIT_QUEUE_LOGGING       = "LOGGING.MESSAGE";
    public static final String RABBIT_QUEUE_CLAIM         = "CLAIM.STATUS";
    public static final String RABBIT_QUEUE_QUESTIONNAIRE = "QUESTIONNAIRE.COMPLETE";
    public static final String RABBIT_QUEUE_ANALYSIS      = "ANALYSIS.RESULT";

    public static final String TEMPLATE_CLAIM = "template-claim-status";
    public static final String TEMPLATE_QUESTIONNAIRE_COMPLETE = "template-questionnaire-complete";
    public static final String TEMPLATE_ANALYSIS_RESULTS = "template-analysis-results";
}
