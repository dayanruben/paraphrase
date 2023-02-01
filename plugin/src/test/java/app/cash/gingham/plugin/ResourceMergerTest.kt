// Copyright Square, Inc.
package app.cash.gingham.plugin

import app.cash.gingham.plugin.model.MergedResource
import app.cash.gingham.plugin.model.PublicResource
import app.cash.gingham.plugin.model.ResourceFolder
import app.cash.gingham.plugin.model.ResourceName
import app.cash.gingham.plugin.model.TokenizedResource
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ResourceMergerTest {

  @Test
  fun emptyPublicResourcesProducesPublicResource() {
    val result = mergeResources(
      name = ResourceName("test"),
      tokenizedResources = mapOf(
        ResourceFolder.Default to TokenizedResource(
          name = ResourceName("test"),
          description = null,
          tokens = emptyList(),
          parsingError = null,
        ),
      ),
      publicResources = emptyList(),
    )
    assertThat(result!!.visibility).isEqualTo(MergedResource.Visibility.Public)
  }

  @Test
  fun inclusionInPublicResourcesProducesPublicResource() {
    val result = mergeResources(
      name = ResourceName("test"),
      tokenizedResources = mapOf(
        ResourceFolder.Default to TokenizedResource(
          name = ResourceName("test"),
          description = null,
          tokens = emptyList(),
          parsingError = null,
        ),
      ),
      publicResources = listOf(
        PublicResource.Named(
          name = ResourceName("test"),
          type = "string",
        )
      ),
    )
    assertThat(result!!.visibility).isEqualTo(MergedResource.Visibility.Public)
  }

  @Test
  fun inclusionInPublicResourcesWithWrongTypeProducesPrivateResource() {
    val result = mergeResources(
      name = ResourceName("test"),
      tokenizedResources = mapOf(
        ResourceFolder.Default to TokenizedResource(
          name = ResourceName("test"),
          description = null,
          tokens = emptyList(),
          parsingError = null,
        ),
      ),
      publicResources = listOf(
        PublicResource.Named(
          name = ResourceName("test"),
          type = "color",
        )
      ),
    )
    assertThat(result!!.visibility).isEqualTo(MergedResource.Visibility.Private)
  }

  @Test
  fun exclusionFromPublicResourcesProducesPrivateResource() {
    val result = mergeResources(
      name = ResourceName("test"),
      tokenizedResources = mapOf(
        ResourceFolder.Default to TokenizedResource(
          name = ResourceName("test"),
          description = null,
          tokens = emptyList(),
          parsingError = null,
        ),
      ),
      publicResources = listOf(
        PublicResource.EmptyDeclaration,
        PublicResource.Named(
          name = ResourceName("different"),
          type = "string",
        )
      ),
    )
    assertThat(result!!.visibility).isEqualTo(MergedResource.Visibility.Private)
  }
}