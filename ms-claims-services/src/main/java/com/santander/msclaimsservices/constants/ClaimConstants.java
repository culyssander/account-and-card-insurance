package com.santander.msclaimsservices.constants;

public final class ClaimConstants {

    private ClaimConstants () {}

    public static final String CLAIM_ERROR_SAVE = "claim.error.save";
    public static final String CLAIM_ERROR_SAVE_ROLLBACK = "claim.error.save.rollback";
    public static final String CLAIM_NOT_FOUND = "claim.not_found";
    public static final String CLAIM_FILE_IS_EMPTY = "claim.file_is_empty";
    public static final String CLAIM_FILE_EXTENSION_NOT_ALLOWED = "claim.file_extension_not_allowed";
    public static final String CLAIM_FILE_INVALID_TYPE = "claim.file_invalid_type";
    public static final String CLAIM_FILE_ONLY_PDF_AND_IMAGE_ARE_ALLOWED = "claim.file_only_pdf_and_image_are_allowed";
    public static final String CLAIM_FILE_EXCEEDS_MAXIMUM = "claim.file_exceeds_maximum";

    public final static String ARGUMENT_INVALID = "argument.invalid";

    public static final String POLICY_ERROR_TO_FIND = "policy.error_to_find";
    public static final String PROTOCOL_ERROR_TO_GET = "protocol.error_to_get";

    public static final String RABBIT_QUEUE_LOGGING_EXCHANGE  = "LOGGING.MESSAGE.EXCHANGE";
    public static final String RABBIT_QUEUE_LOGGING_ROUTER    = "LOGGING.MESSAGE.KEY";
    public static final String RABBIT_QUEUE_LOGGING           = "LOGGING.MESSAGE";

    public static final String RABBIT_QUEUE_CLAIM_EXCHANGE  = "CLAIM.CREATED.EXCHANGE";
    public static final String RABBIT_QUEUE_CLAIM_ROUTER    = "CLAIM.CREATED.KEY";
    public static final String RABBIT_QUEUE_CLAIM           = "CLAIM.CREATED";

    public static final String RABBIT_QUEUE_NOTIFICATION_EXCHANGE  = "NOTIFICATION.REQUEST.EXCHANGE";
    public static final String RABBIT_QUEUE_NOTIFICATION_ROUTER    = "NOTIFICATION.REQUEST.KEY";
    public static final String RABBIT_QUEUE_NOTIFICATION           = "NOTIFICATION.REQUEST";
}
