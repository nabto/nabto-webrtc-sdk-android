/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package org.openapitools.client.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 
 *
 * @param errorCode 
 * @param errorMessage 
 */


data class PostTestClientByTestIdSendDeviceErrorRequest (

    @Json(name = "errorCode")
    val errorCode: kotlin.String,

    @Json(name = "errorMessage")
    val errorMessage: kotlin.String? = null

) {


}

