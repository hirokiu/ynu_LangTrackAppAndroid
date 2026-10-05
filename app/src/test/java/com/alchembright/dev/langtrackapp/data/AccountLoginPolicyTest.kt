package com.alchembright.dev.langtrackapp.data

import org.junit.Assert.*
import org.junit.Test

class AccountLoginPolicyTest {
    @Test fun explicitCapabilitiesSelectAuthentication() {
        assertEquals(AccountLoginPolicy.Route.USERNAME, AccountLoginPolicy.options(200, true))
        assertEquals(AccountLoginPolicy.Route.LEGACY, AccountLoginPolicy.options(200, false))
        assertEquals(AccountLoginPolicy.Route.LEGACY, AccountLoginPolicy.options(404, null))
    }
    @Test fun brokenCapabilitiesNeverDowngradeAuthentication() {
        listOf(0, 401, 429, 500, 503).forEach {
            assertEquals(AccountLoginPolicy.Route.FAIL, AccountLoginPolicy.options(it, false))
        }
        assertEquals(AccountLoginPolicy.Route.FAIL, AccountLoginPolicy.options(200, null))
    }
    @Test fun onlyRejectedUsernameCredentialsAllowLegacyFallback() {
        assertTrue(AccountLoginPolicy.allowLegacyFallback(401))
        listOf(0, 200, 400, 403, 404, 429, 500, 503).forEach {
            assertFalse(AccountLoginPolicy.allowLegacyFallback(it))
        }
    }
}
