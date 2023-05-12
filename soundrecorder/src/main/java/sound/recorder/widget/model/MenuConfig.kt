package sound.recorder.widget.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
class MenuConfig : Serializable {
    var appName : String? =null
    var versionName : String? =null
    var versionCode : Int? =  null
    var forceUpdate : Boolean? = null
    var maintenance : Boolean? =null
}