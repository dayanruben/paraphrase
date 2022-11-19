package app.cash.gingham

import android.content.res.Resources

/**
 * A [FormattedStringResource] consists of:
 * 1. An Android string resource
 * 2. The arguments required to resolve it
 *
 * For example, if the following string was declared in the strings.xml resource file:
 * ```xml
 * <string name="detective_has_suspects">
 *   {suspects, plural,
 *     =0 {{detective} has no suspects}
 *     =1 {{detective} has one suspect}
 *     other {{detective} has # suspects}
 *   }
 * </string>
 * ```
 *
 * The [FormattedStringResource] would contain:
 * - The R.string.detective_has_suspects resource ID
 * - An integer value for the suspects argument
 * - A string value for the detective argument
 */
fun interface FormattedStringResource {
  /**
   * Resolves the final formatted version of the string by:
   * 1. Using [Resources] to look up the string pattern
   * 2. Inserting argument values into the pattern
   */
  fun resolve(resources: Resources): String
}