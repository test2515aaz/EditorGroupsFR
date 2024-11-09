/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package krasa.editorGroups.index

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndex.ValueProcessor

@Service(Service.Level.APP)
class FileNameIndexService {
  /**
   * Retrieves a collection of virtual files by their name within a given project scope.
   *
   * @param name the name of the files to search for.
   * @param scope the scope within which to search for the files.
   * @return a mutable collection of virtual files that match the specified name and scope.
   */
  fun getVirtualFilesByName(name: String, scope: GlobalSearchScope): MutableCollection<VirtualFile> {
    val files: MutableSet<VirtualFile> = mutableSetOf<VirtualFile>()

    FileBasedIndex.getInstance()
      .processValues<String?, Void?>(
        FilenameWithoutExtensionIndex.NAME,
        name,
        null,
        ValueProcessor { file: VirtualFile?, value: Void? ->
          files.add(file!!)
          true
        },
        scope,
        null
      )
    return files
  }

  /**
   * Retrieves a collection of virtual files by their name, ignoring case sensitivity, within a given project scope.
   *
   * @param name the name of the files to search for.
   * @param scope the scope within which to search for the files.
   * @return a mutable set of virtual files that match the specified name (ignoring case) and scope.
   */
  private fun getVirtualFilesByNameIgnoringCase(name: String, scope: GlobalSearchScope): MutableSet<VirtualFile> {
    val keys: MutableSet<String> = mutableSetOf<String>()

    // Retrieve all files related with name, ignoring case
    processAllFileNames(
      Processor { fileName: String? ->
        if (name.equals(fileName, ignoreCase = true)) keys.add(fileName!!)
        true
      },
      scope
    )

    // values accessed outside of processAllKeys
    val files: MutableSet<VirtualFile> = mutableSetOf()
    keys.forEach { key -> files.addAll(getVirtualFilesByName(name = key, scope = scope)) }
    return files
  }

  /**
   * Retrieves a collection of virtual files by their name within a given project scope.
   *
   * @param name the name of the files to search for.
   * @param caseSensitively whether the search should be case-sensitive.
   * @param scope the scope within which to search for the files.
   * @return a mutable collection of virtual files that match the specified name and scope.
   */
  fun getVirtualFilesByName(name: String, caseSensitively: Boolean, scope: GlobalSearchScope): MutableCollection<VirtualFile> = when {
    caseSensitively -> getVirtualFilesByName(name, scope)
    else            -> getVirtualFilesByNameIgnoringCase(name, scope)
  }

  /**
   * Processes all file names within the specified scope using the given processor.
   *
   * @param processor the processor to be applied to each file name.
   * @param scope the scope within which to search for the file names.
   */
  private fun processAllFileNames(processor: Processor<String?>, scope: GlobalSearchScope) {
    FileBasedIndex.getInstance().processAllKeys<String?>(
      FilenameWithoutExtensionIndex.NAME,
      processor,
      scope,
      null
    )
  }

  companion object {
    @JvmStatic
    val instance: FileNameIndexService by lazy { service() }
  }
}
