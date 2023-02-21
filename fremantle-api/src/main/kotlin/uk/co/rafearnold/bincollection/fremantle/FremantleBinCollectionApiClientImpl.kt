package uk.co.rafearnold.bincollection.fremantle

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

internal class FremantleBinCollectionApiClientImpl @Inject constructor(
    private val appProps: Map<String, String>
) : FremantleBinCollectionApiClient {

    private val httpClient: HttpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()

    private val basePath: URI get() = URI(appProps.getValue("fremantle-collection-api.base-path"))

    override fun getAddressData(query: String): CompletableFuture<GetAddressDataResponse> =
        doRequest(
            path = "/bindatefinder/getaddresses.ashx" + buildQuerySuffix(mapOf("addr" to query)),
            method = "GET"
        )
            .thenApply { responseBody: String ->
                try {
                    val json: JSONArray = JSONParser().parse(responseBody) as JSONArray
                    GetAddressDataResponse(
                        json.map { addressJson: Any? ->
                            addressJson as JSONObject
                            AddressData(
                                polygonNu = addressJson["polygon_nu"] as String,
                                address = addressJson["address"] as String,
                            )
                        }
                    )
                } catch (it: Throwable) {
                    throw HttpResponseBodyParseException(body = responseBody, schemaName = "address data", cause = it)
                }
            }


    override fun getBinCollectionData(addressData: AddressData): CompletableFuture<GetBinCollectionDataResponse> =
        doRequest(
            path = "/bindatefinder/getbindate.ashx" + buildQuerySuffix(mapOf("polygon" to addressData.polygonNu)),
            method = "GET"
        )
            .thenApply { responseBody: String ->
                try {
                    val json: JSONArray = JSONParser().parse(responseBody) as JSONArray
                    GetBinCollectionDataResponse(
                        dataList = json
                            .map { collectionElement: Any? ->
                                collectionElement as JSONObject
                                BinCollectionData(
                                    name = collectionElement["name"] as String,
                                    details = collectionElement["details"] as String,
                                )
                            }
                    )
                } catch (it: Throwable) {
                    throw HttpResponseBodyParseException(
                        body = responseBody, schemaName = "bin collection data", cause = it,
                    )
                }
            }

    private fun buildQuerySuffix(params: Map<String, String>): String =
        "?" + params.map { (name: String, value: String) -> encode(name) + "=" + encode(value) }
            .joinToString(separator = "&")

    private fun encode(s: String): String = URLEncoder.encode(s, Charsets.UTF_8)

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
                        body = httpResponse.body(),
                    )
                } else httpResponse.body()
            }
    }
}
