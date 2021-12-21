package uk.gov.justice.digital.hmpps.prisontonhs.services

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDate

@ApiModel(description = "Prisoner Information for NHS")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class NhsPrisoner(
  @ApiModelProperty(
    value = "Offender Identifier",
    example = "A1234AA",
    required = true,
    position = 1
  ) val nomsId: String,
  @ApiModelProperty(
    value = "GP Practice Code",
    example = "Y05537",
    required = true,
    position = 2
  ) val gpPracticeCode: String,
  @ApiModelProperty(
    value = "Establishment Code for prisoner",
    example = "MDI",
    required = true,
    position = 3
  ) val establishmentCode: String,
  @ApiModelProperty(value = "Given Name 1", example = "John", required = true, position = 4) val givenName1: String,
  @ApiModelProperty(value = "Given Name 2", example = "Luke", position = 5) val givenName2: String?,
  @ApiModelProperty(value = "Last Name", example = "Smith", required = true, position = 6) val lastName: String,
  @ApiModelProperty(value = "Requested Name", example = "Dave", position = 7) val requestedName: String?,
  @ApiModelProperty(
    value = "Date of Birth",
    example = "1970-05-01",
    required = true,
    position = 8
  ) val dateOfBirth: LocalDate,
  @ApiModelProperty(value = "Gender", example = "Male", required = true, position = 9) val gender: String,
  @ApiModelProperty(
    value = "Indicated that is English speaking",
    example = "true",
    required = true,
    position = 10
  ) val englishSpeaking: Boolean,
  @ApiModelProperty(value = "Level 1 Location Unit Code", example = "A", position = 11) val unitCode1: String?,
  @ApiModelProperty(value = "Level 2 Location Unit Code", example = "2", position = 12) val unitCode2: String?,
  @ApiModelProperty(value = "Level 3 Location Unit Code", example = "003", position = 13) val unitCode3: String?,
  @ApiModelProperty(
    value = "Date Prisoner booking was initial made",
    example = "2017-05-01",
    position = 14
  ) val bookingBeginDate: LocalDate?,
  @ApiModelProperty(
    value = "Date of admission into this prison",
    example = "2019-06-01",
    required = true,
    position = 15
  ) val admissionDate: LocalDate?,
  @ApiModelProperty(
    value = "Confirmed, actual, approved, provisional or calculated release date for offender, according to offender release date algorithm." +
      "<h3>Algorithm</h3><ul><li>If there is a confirmed release date, the offender release date is the confirmed release date.</li><li>If there is no confirmed release date for the offender, the offender release date is either the actual parole date or the home detention curfew actual date.</li><li>If there is no confirmed release date, actual parole date or home detention curfew actual date for the offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both are present)</li></ul>",
    example = "2021-04-12",
    position = 16
  ) val releaseDate: LocalDate?,
  @ApiModelProperty(value = "Category of this prisoner", example = "C", position = 17) val categoryCode: String?,
  @ApiModelProperty(
    value = "Status of prisoner in community",
    required = true,
    example = "ACTIVE IN",
    allowableValues = "ACTIVE IN,ACTIVE OUT,INACTIVE OUT",
    position = 18
  ) val communityStatus: String,
  @ApiModelProperty(
    value = "Legal Status",
    example = "SENTENCED",
    allowableValues = "RECALL,DEAD,INDETERMINATE_SENTENCE,SENTENCED,CONVICTED_UNSENTENCED,CIVIL_PRISONER,IMMIGRATION_DETAINEE,REMAND,UNKNOWN,OTHER",
    position = 19
  ) val legalStatus: String?
)
