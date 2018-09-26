package com.binxhealth.hermes.test.app.utils

import com.stehno.ersatz.ContentType
import com.stehno.ersatz.Decoders
import com.stehno.ersatz.ErsatzServer

class TestUtils {

    /**
     * Ersatz provides a default application/json decoder, but does not associate the decoder with the
     * 'application/json' content type by default (for some reason...), so we have to do it manually.
     * @return new ErsatzServer instance with the application/json decoder appropriately mapped
     */
    static ErsatzServer newErsatzServer() {
        ErsatzServer mock = new ErsatzServer()
        mock.decoder(ContentType.APPLICATION_JSON.value, Decoders.parseJson)
        return mock
    }

}
