package au.org.ala.cas.webflow

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CodedName @JsonCreator constructor(
    @param:JsonProperty val isoCode: String,
    @param:JsonProperty val name: String
)