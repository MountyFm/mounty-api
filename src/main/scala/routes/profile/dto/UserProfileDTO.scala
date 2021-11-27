package routes.profile.dto

import kz.mounty.fm.domain.user.UserProfile
import org.joda.time.DateTime

import java.util.UUID

case class UserProfileDTO(name: String,
                          email: String,
                          avatarUrl: Option[String] = None,
                          spotifyUri: String)

object UserProfileDTO {
  def convert(userProfileDTO: UserProfileDTO): UserProfile = {
    UserProfile(
      id = UUID.randomUUID().toString,
      name = userProfileDTO.name,
      email = userProfileDTO.email,
      avatarUrl = userProfileDTO.avatarUrl,
      spotifyUri = userProfileDTO.spotifyUri,
      createdAt = DateTime.now()
    )
  }

  def convert(userProfile: UserProfile): UserProfileDTO = {
    UserProfileDTO(
      name = userProfile.name,
      email = userProfile.email,
      avatarUrl = userProfile.avatarUrl,
      spotifyUri = userProfile.spotifyUri
    )
  }
}
