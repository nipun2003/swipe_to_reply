package com.nipunapps.testcode

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class ResourceComparerTest{
    private lateinit var resourceComparer: ResourceComparer

    @Before
    fun setup(){
        resourceComparer = ResourceComparer()
    }
    @Test
    fun stringResourceSameAsGivenString_ReturnTrue(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = resourceComparer.isEqual(context,R.string.app_name,"TestCode")
        assertThat(result).isTrue()
    }

    @Test
    fun stringResourceDifferentAsGivenString_ReturnFalse(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = resourceComparer.isEqual(context,R.string.app_name,"Test Code")
        assertThat(result).isFalse()
    }
}