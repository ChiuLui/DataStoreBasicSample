package com.chiului.datastorebasicsample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import com.chiului.datastorebasicsample.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var dataStore: DataStore<Preferences>

    private lateinit var MY_CONTENT: Preferences.Key<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mBinding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        mBinding.lifecycleOwner = this
        mBinding.clickListener = this

        dataStore = createDataStore(
            name = "settings"
        )

        MY_CONTENT = preferencesKey<String>("my_content")
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_write -> {
                // 异步写数据
                GlobalScope.launch {
                    write()
                }
            }
            R.id.btn_read -> {
                // 异步读取（订阅）数据
                GlobalScope.launch {
                    read()
                }
            }
        }
    }

    private suspend fun read() {
        val content: Flow<String> = dataStore.data
            .map { currentPreferences ->
                // 不同于 Proto DataStore，这里不保证类型安全。
                currentPreferences[MY_CONTENT] ?: ""
            }
        content.collect {
            mBinding.txRead.text = it
        }
    }

    private suspend fun write() {
        dataStore.edit { settings ->
            // 可以安全地增加我们的计数器，而不会因为资源竞争而丢失数据。
            settings[MY_CONTENT] = mBinding.edWrite.text.toString()
        }
    }
}