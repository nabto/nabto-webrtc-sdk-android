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
 * @param code 
 * @param message 
 */


data class PostTestClientByTestIdWaitForDeviceError200ResponseError (

    @Json(name = "code")
    val code: kotlin.String,

    @Json(name = "message")
    val message: kotlin.String? = null

) {


}

