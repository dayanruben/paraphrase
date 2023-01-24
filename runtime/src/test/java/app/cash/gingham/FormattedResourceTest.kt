package app.cash.gingham

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FormattedResourceTest {
  //region equals
  @Test fun `equals same instance returns true`() {
    val instance = FormattedResource(id = 123, arguments = intArrayOf(1, 2))
    @Suppress("KotlinConstantConditions")
    assertThat(instance == instance).isTrue()
  }

  @Test fun `equals instance of different class returns false`() {
    val instance = FormattedResource(id = 123, arguments = 1)
    assertThat(instance.equals(listOf("a"))).isFalse()
  }

  @Test fun `equals with different ids returns false`() {
    val a = FormattedResource(id = 123, arguments = mapOf("a" to 1, "b" to 2))
    val b = FormattedResource(id = 321, arguments = mapOf("a" to 1, "b" to 2))
    assertThat(a == b).isFalse()
    assertThat(b == a).isFalse()
  }

  @Test fun `equals with different argument maps returns false`() {
    val a = FormattedResource(id = 123, arguments = mapOf("a" to 1, "b" to 2))
    val b = FormattedResource(id = 123, arguments = mapOf("a" to 3, "b" to 4))
    assertThat(a == b).isFalse()
    assertThat(b == a).isFalse()
  }

  @Test fun `equals with same ids and argument maps returns true`() {
    val a = FormattedResource(id = 123, arguments = mapOf("a" to 1, "b" to 2))
    val b = FormattedResource(id = 123, arguments = mapOf("a" to 1, "b" to 2))
    assertThat(a == b).isTrue()
    assertThat(b == a).isTrue()
  }

  @Test fun `equals with different argument arrays returns false`() {
    val a = FormattedResource(id = 123, arguments = arrayOf("a", "b"))
    val b = FormattedResource(id = 123, arguments = arrayOf("c", "d"))
    assertThat(a == b).isFalse()
    assertThat(b == a).isFalse()
  }

  @Test fun `equals with only one argument array returns false`() {
    val a = FormattedResource(id = 123, arguments = arrayOf("a", "b"))
    val b = FormattedResource(id = 123, arguments = "cd")
    assertThat(a == b).isFalse()
    assertThat(b == a).isFalse()
  }

  @Test fun `equals with same ids and argument arrays returns true`() {
    val a = FormattedResource(id = 123, arguments = arrayOf("a", "b"))
    val b = FormattedResource(id = 123, arguments = arrayOf("a", "b"))
    assertThat(a == b).isTrue()
    assertThat(b == a).isTrue()
  }
  //endregion

  //region hashCode
  @Test fun `hashCode with different ids returns different values`() {
    val a = FormattedResource(id = 123, arguments = mapOf("a" to 1, "b" to 2))
    val b = FormattedResource(id = 321, arguments = mapOf("a" to 1, "b" to 2))
    assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
  }

  @Test fun `hashCode with different argument maps returns different values`() {
    val a = FormattedResource(id = 123, arguments = mapOf("a" to 1, "b" to 2))
    val b = FormattedResource(id = 123, arguments = mapOf("a" to 3, "b" to 4))
    assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
  }

  @Test fun `hashCode with same ids and argument maps returns same values`() {
    val a = FormattedResource(id = 123, arguments = mapOf("a" to 1, "b" to 2))
    val b = FormattedResource(id = 123, arguments = mapOf("a" to 1, "b" to 2))
    assertThat(a.hashCode()).isEqualTo(b.hashCode())
  }

  @Test fun `hashCode with different argument arrays returns different values`() {
    val a = FormattedResource(id = 123, arguments = arrayOf("a", "b"))
    val b = FormattedResource(id = 123, arguments = arrayOf("c", "d"))
    assertThat(a.hashCode()).isNotEqualTo(b.hashCode())
  }

  @Test fun `hashCode with same ids and argument arrays returns same values`() {
    val a = FormattedResource(id = 123, arguments = arrayOf("a", "b"))
    val b = FormattedResource(id = 123, arguments = arrayOf("a", "b"))
    assertThat(a.hashCode()).isEqualTo(b.hashCode())
  }
  //endregion

  //region toString
  @Test fun `toString with map includes contents`() {
    val instance = FormattedResource(id = 123, arguments = mapOf("a" to 1, "b" to 2))
    assertThat(instance.toString()).isEqualTo("FormattedResource(id=123, arguments={a=1, b=2}")
  }

  @Test fun `toString with array includes contents`() {
    val instance = FormattedResource(id = 123, arguments = arrayOf("a", "b"))
    assertThat(instance.toString()).isEqualTo("FormattedResource(id=123, arguments=[a, b]")
  }
  //endregion
}
