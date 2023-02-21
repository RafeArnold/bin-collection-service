package uk.co.rafearnold.bincollection.fremantle

sealed class FremantleBinCollectionApiClientException(
    override val message: String,
    override val cause: Throwable?
) : Throwable(message, cause)

class UnsuccessfulHttpResponseException(
    statusCode: Int,
    body: String
) : FremantleBinCollectionApiClientException(
    message = "Unsuccessful HTTP response. Code: '$statusCode', body: '$body'",
    cause = null
)

class HttpResponseBodyParseException(
    body: String,
    schemaName: String,
    cause: Throwable? = null
) : FremantleBinCollectionApiClientException(
    message = "Could not parse HTTP response body to $schemaName: '$body'",
    cause = cause
)
