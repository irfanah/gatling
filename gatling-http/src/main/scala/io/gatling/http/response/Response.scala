/**
 * Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.http.response

import java.nio.{ ByteBuffer, CharBuffer }

import scala.collection.JavaConversions.asScalaBuffer

import com.ning.http.client.{ Request, Response => AHCResponse }

import io.gatling.core.config.GatlingConfiguration.configuration
import io.gatling.core.util.BytesInputStream
import io.gatling.core.util.StringHelper.RichString

trait Response extends AHCResponse {

	def request: Request
	def ahcResponse: Option[AHCResponse]
	def checksums: Map[String, String]
	def firstByteSent: Long
	def lastByteSent: Long
	def firstByteReceived: Long
	def lastByteReceived: Long
	def checksum(algorithm: String): Option[String]
	def reponseTimeInMillis: Long
	def latencyInMillis: Long
	def isReceived: Boolean
	def getHeaderSafe(name: String): Option[String]
	def getHeadersSafe(name: String): Seq[String]
	def charBuffer: CharBuffer
	def chars: Array[Char]
}

case class HttpResponse(
	request: Request,
	ahcResponse: Option[AHCResponse],
	checksums: Map[String, String],
	firstByteSent: Long,
	lastByteSent: Long,
	firstByteReceived: Long,
	lastByteReceived: Long,
	bytes: Array[Byte]) extends Response {

	def checksum(algorithm: String) = checksums.get(algorithm)
	def reponseTimeInMillis = lastByteReceived - firstByteSent
	def latencyInMillis = firstByteReceived - firstByteReceived
	def isReceived = ahcResponse.isDefined
	def getHeaderSafe(name: String): Option[String] = ahcResponse.flatMap(r => Option(r.getHeader(name)))
	def getHeadersSafe(name: String): Seq[String] = ahcResponse.flatMap(r => Option(r.getHeaders(name))).map(_.toSeq).getOrElse(Nil)

	lazy val string = new String(bytes, configuration.core.charSet)
	lazy val charBuffer = configuration.core.charSet.decode(ByteBuffer.wrap(bytes))
	lazy val chars = string.unsafeChars

	override def toString = ahcResponse.toString

	private def receivedResponse = ahcResponse.getOrElse(throw new IllegalStateException("Response was not built"))
	def getStatusCode = receivedResponse.getStatusCode
	def getStatusText = receivedResponse.getStatusText
	def getResponseBodyAsBytes = bytes
	def getResponseBodyAsStream = new BytesInputStream(bytes)
	def getResponseBodyAsByteBuffer = throw new UnsupportedOperationException
	def getResponseBodyExcerpt(maxLength: Int, charset: String) = throw new UnsupportedOperationException
	def getResponseBody(charset: String) = new String(bytes, charset)
	def getResponseBodyExcerpt(maxLength: Int) = throw new UnsupportedOperationException
	def getResponseBody = string
	def getUri = receivedResponse.getUri
	def getContentType = receivedResponse.getContentType
	def getHeader(name: String) = receivedResponse.getHeader(name)
	def getHeaders(name: String) = receivedResponse.getHeaders(name)
	def getHeaders = receivedResponse.getHeaders
	def isRedirected = ahcResponse.map(_.isRedirected).getOrElse(false)
	def getCookies = receivedResponse.getCookies
	def hasResponseStatus = ahcResponse.map(_.hasResponseStatus).getOrElse(false)
	def hasResponseHeaders = ahcResponse.map(_.hasResponseHeaders).getOrElse(false)
	def hasResponseBody = bytes.length != 0
}

class DelegatingReponse(delegate: Response) extends Response {

	def request: Request = delegate.request
	def ahcResponse = delegate.ahcResponse
	def checksums = delegate.checksums
	def firstByteSent = delegate.firstByteSent
	def lastByteSent = delegate.lastByteSent
	def firstByteReceived = delegate.firstByteReceived
	def lastByteReceived = delegate.lastByteReceived
	def checksum(algorithm: String) = delegate.checksum(algorithm)
	def reponseTimeInMillis = delegate.reponseTimeInMillis
	def latencyInMillis = delegate.latencyInMillis
	def isReceived = delegate.isReceived
	def getHeaderSafe(name: String) = delegate.getHeaderSafe(name)
	def getHeadersSafe(name: String) = delegate.getHeadersSafe(name)

	def getStatusCode = delegate.getStatusCode
	def getStatusText = delegate.getStatusText
	def getResponseBodyAsBytes = delegate.getResponseBodyAsBytes
	def getResponseBodyAsStream = delegate.getResponseBodyAsStream
	def getResponseBodyAsByteBuffer = delegate.getResponseBodyAsByteBuffer
	def getResponseBodyExcerpt(maxLength: Int, charset: String) = delegate.getResponseBodyExcerpt(maxLength, charset)
	def getResponseBody(charset: String) = delegate.getResponseBody(charset)
	def getResponseBodyExcerpt(maxLength: Int) = delegate.getResponseBodyExcerpt(maxLength)
	def getResponseBody = delegate.getResponseBody
	def getUri = delegate.getUri
	def getContentType = delegate.getContentType
	def getHeader(name: String) = delegate.getHeader(name)
	def getHeaders(name: String) = delegate.getHeaders(name)
	def getHeaders = delegate.getHeaders
	def isRedirected = delegate.isRedirected
	def getCookies = delegate.getCookies
	def hasResponseStatus = delegate.hasResponseStatus
	def hasResponseHeaders = delegate.hasResponseHeaders
	def hasResponseBody = delegate.hasResponseHeaders
	def charBuffer = delegate.charBuffer
	def chars = delegate.chars
}