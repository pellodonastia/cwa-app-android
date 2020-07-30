package de.rki.coronawarnapp.http

import de.rki.coronawarnapp.service.submission.KeyType
import de.rki.coronawarnapp.util.newWebRequestBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.utf8Size
import org.junit.Assert
import org.junit.Test


class VerificationServiceTest {

    @Test
    fun allRequestHaveSameFootprintForPlausibleDeniability(): Unit = runBlocking {

        val server = MockWebServer()
        server.start()

        val webRequestBuilder = server.newWebRequestBuilder()

        val guidExample = "3BF1D4-1C6003DD-733D-41F1-9F30-F85FA7406BF7"
        val teletanExample = "9A3B578UMG"
        val registrationTokenExample = "63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f"

        server.enqueue(MockResponse().setBody("{}"))
        webRequestBuilder.asyncGetRegistrationToken(guidExample, KeyType.GUID)

        server.enqueue(MockResponse().setBody("{}"))
        webRequestBuilder.asyncGetRegistrationToken(teletanExample, KeyType.TELETAN)

        server.enqueue(MockResponse().setBody("{}"))
        webRequestBuilder.asyncFakeGetRegistrationToken()

        server.enqueue(MockResponse().setBody("{}"))
        webRequestBuilder.asyncGetTestResult(registrationTokenExample)

        server.enqueue(MockResponse().setBody("{}"))
        webRequestBuilder.asyncFakeGetTestResult()

        server.enqueue(MockResponse().setBody("{}"))
        webRequestBuilder.asyncGetTan(registrationTokenExample)

        server.enqueue(MockResponse().setBody("{}"))
        webRequestBuilder.asyncFakeGetTan()

        val requests = listOf(
            server.takeRequest(),
            server.takeRequest(),
            server.takeRequest(),
            server.takeRequest(),
            server.takeRequest(),
            server.takeRequest(),
            server.takeRequest()
        )

        // ensure all request have same size (header & body)
        requests.zipWithNext().forEach { (a, b) ->
            Assert.assertEquals(
                "Header size mismatch: ",
                a.requestHeaderSize(),
                b.requestHeaderSize()
            )
            Assert.assertEquals("Body size mismatch:", a.bodySize, b.bodySize)
        }
    }
}

private fun RecordedRequest.requestHeader() =
    listOf(this.requestLine).plus(this.headers.toString()).joinToString("\n")

private fun RecordedRequest.requestHeaderSize() = requestHeader().utf8Size()