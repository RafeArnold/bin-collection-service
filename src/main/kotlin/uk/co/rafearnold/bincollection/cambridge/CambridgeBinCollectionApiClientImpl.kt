package uk.co.rafearnold.bincollection.cambridge

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class CambridgeBinCollectionApiClientImpl @Inject constructor(
    private val appProps: Map<String, String>
) : CambridgeBinCollectionApiClient {

    private val httpClient: HttpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()

    private val basePath: URI get() = URI(appProps.getValue("cambridge-collection-api.base-path"))

    override fun getPostcodeData(postcode: String): CompletableFuture<GetPostcodeDataResponse> =
        doRequest(
            path = "/wastecalendar/address/search" + buildQuerySuffix(mapOf("postcode" to postcode)),
            method = "GET"
        )
            .thenApply { responseBody: String ->
                try {
                    val json: JSONArray = JSONParser().parse(responseBody) as JSONArray
                    GetPostcodeDataResponse(
                        json.map { addressJson: Any? ->
                            addressJson as JSONObject
                            AddressData(
                                id = addressJson["id"] as String,
                                houseNumber = addressJson["houseNumber"] as String
                            )
                        }
                    )
                } catch (it: Throwable) {
                    throw HttpResponseBodyParseException(
                        body = responseBody, schemaName = "postcode data", cause = it
                    )
                }
            }


    override fun getBinCollectionData(addressId: String): CompletableFuture<GetBinCollectionDataResponse> =
        doRequest(path = "/wastecalendar/collection/search/$addressId", method = "GET")
            .thenApply { responseBody: String ->
                try {
                    val json: JSONObject = JSONParser().parse(responseBody) as JSONObject
                    GetBinCollectionDataResponse(
                        collections = (json["collections"] as JSONArray)
                            .map { collectionElement: Any? ->
                                collectionElement as JSONObject
                                BinCollectionData(
                                    date = ZonedDateTime.parse(collectionElement["date"] as String),
                                    roundTypes = (collectionElement["roundTypes"] as JSONArray)
                                        .map { roundType: Any? ->
                                            RoundType.values().first { it.name == roundType as String }
                                        }.toSet()
                                )
                            }
                    )
                } catch (it: Throwable) {
                    throw HttpResponseBodyParseException(
                        body = responseBody, schemaName = "bin collection data", cause = it
                    )
                }
            }

    private fun buildQuerySuffix(params: Map<String, String>): String =
        "?" + params.map { (name: String, value: String) -> "$name=$value" }.joinToString(separator = "&")

    private fun doRequest(
        path: String,
        method: String
    ): CompletableFuture<String> {
        val request: HttpRequest =
            HttpRequest.newBuilder()
                .uri(basePath.resolve(path))
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build()
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApplyAsync { httpResponse: HttpResponse<String> ->
                if (httpResponse.statusCode() != 200) {
                    throw UnsuccessfulHttpResponseException(
                        statusCode = httpResponse.statusCode(),
                        body = httpResponse.body()
                    )
                } else httpResponse.body()
            }
    }
}
