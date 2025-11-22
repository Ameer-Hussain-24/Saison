package takagi.ru.saison

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SaisonApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
