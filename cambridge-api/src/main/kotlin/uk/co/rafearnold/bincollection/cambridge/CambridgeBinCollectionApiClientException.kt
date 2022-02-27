package uk.co.rafearnold.bincollection.cambridge

sealed class CambridgeBinCollectionApiClientException(
    override val message: String,
    override val cause: Throwable?
) : Throwable(message, cause)

class UnsuccessfulHttpResponseException(
    statusCode: Int,
    body: String
) : CambridgeBinCollectionApiClientException(
    message = "Unsuccessful HTTP response. Code: '$statusCode', body: '$body'",
    cause = null
)

class HttpResponseBodyParseException(
    body: String,
    schemaName: String,
    cause: Throwable? = null
) : CambridgeBinCollectionApiClientException(
    message = "Could not parse HTTP response body to $schemaName: '$body'",
    cause = cause
)
