package sound.recorder.widget.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
class MenuConfig : Serializable {
    var app_name : String? =null
    var app_info :  AndroidInfo? =null

    @JsonIgnoreProperties(ignoreUnknown = true)
    class AndroidInfo : Serializable {
        var version_name : String? =null
        var version_code : Int? =  null
        var force_update : Boolean? = null
        var maintenance : Boolean? =null
    }
}