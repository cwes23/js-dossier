// Copyright 2013 Jason Leyba
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.github.jleyba.dossier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.intersection;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.javascript.jscomp.ErrorManager;
import com.google.javascript.jscomp.PrintStreamErrorManager;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.deps.DependencyInfo;
import com.google.javascript.jscomp.deps.DepsFileParser;
import com.google.javascript.jscomp.deps.DepsGenerator;
import com.google.javascript.jscomp.deps.SortedDependencies;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;

/**
 * Describes the runtime configuration for the app.
 */
class Config {

  private final ImmutableSet<Path> srcs;
  private final ImmutableSet<Path> modules;
  private final ImmutableSet<Path> externs;
  private final Path srcPrefix;
  private final Path modulePrefix;
  private final Path output;
  private final Optional<Path> license;
  private final Optional<Path> readme;
  private final boolean strict;
  private final Language language;
  private final PrintStream outputStream;
  private final PrintStream errorStream;

  /**
   * Creates a new runtime configuration.
   *
   *
   * @param srcs The list of compiler input sources.
   * @param modules The list of CommonJS compiler input sources.
   * @param externs The list of extern files for the Closure compiler.
   * @param output Path to the output directory.
   * @param license Path to a license file to include with the generated documentation.
   * @param readme Path to a markdown file to include in the main index.
   * @param modulePrefix Prefix to strip from each module path when rendering documentation.
   * @param strict Whether to enable all type checks.
   * @param language The JavaScript dialog sources must conform to.
   * @param outputStream The stream to use for standard output.
   * @param errorStream The stream to use for error output.
   * @throws IllegalStateException If any of source, moudle, and extern sets intersect, or if the
   *     output path is not a directory.
   */
  private Config(
      ImmutableSet<Path> srcs, ImmutableSet<Path> modules, ImmutableSet<Path> externs, Path output,
      Optional<Path> license, Optional<Path> readme, Optional<Path> modulePrefix,
      boolean strict, Language language, PrintStream outputStream, PrintStream errorStream) {
    checkArgument(intersection(srcs, externs).isEmpty(),
        "The sources and externs inputs must be disjoint:\n  sources: %s\n  externs: %s",
        srcs, externs);
    checkArgument(intersection(srcs, modules).isEmpty(),
        "The sources and modules inputs must be disjoint:\n  sources: %s\n  modules: %s",
        srcs, modules);
    checkArgument(intersection(modules, externs).isEmpty(),
        "The sources and modules inputs must be disjoint:\n  modules: %s\n  externs: %s",
        modules, externs);
    checkArgument(!Files.exists(output) || Files.isDirectory(output),
        "Output path, %s, is not a directory", output);
    checkArgument(!license.isPresent() || Files.exists(license.get()),
        "LICENSE path, %s, does not exist", license.orNull());
    checkArgument(!readme.isPresent() || Files.exists(readme.get()),
        "README path, %s, does not exist", readme.orNull());

    this.srcs = srcs;
    this.modules = modules;
    this.srcPrefix = getSourcePrefixPath(srcs, modules);
    this.modulePrefix = getModulePreixPath(modulePrefix, modules);
    this.externs = externs;
    this.output = output;
    this.license = license;
    this.readme = readme;
    this.strict = strict;
    this.language = language;
    this.outputStream = outputStream;
    this.errorStream = errorStream;
  }

  /**
   * Returns the set of input sources for the compiler.
   */
  ImmutableSet<Path> getSources() {
    return srcs;
  }

  /**
   * Returns the set of CommonJS input sources for the compiler.
   */
  ImmutableSet<Path> getModules() {
    return modules;
  }

  /**
   * Returns the longest common path prefix for all of the input sources.
   */
  Path getSrcPrefix() {
    return srcPrefix;
  }

  /**
   * Returns the common path prefix for all of the input modules.
   */
  Path getModulePrefix() {
    return modulePrefix;
  }

  /**
   * Returns the set of extern files to use.
   */
  ImmutableSet<Path> getExterns() {
    return externs;
  }

  /**
   * Returns the path to the output directory.
   */
  Path getOutput() {
    return output;
  }

  /**
   * Returns the path to a license file to include with the generated documentation.
   */
  Optional<Path> getLicense() {
    return license;
  }

  /**
   * Returns the path to the readme markdown file, if any, to include in the main index.
   */
  Optional<Path> getReadme() {
    return readme;
  }

  /**
   * Returns whether to enable all type checks.
   */
  boolean isStrict() {
    return strict;
  }

  /**
   * Returns the language dialect sources must conform to.
   */
  Language getLanguage() {
    return language;
  }

  /**
   * Returns the stream to use as stdout.
   */
  PrintStream getOutputStream() {
    return outputStream;
  }

  /**
   * Returns the stream to use as stderr.
   */
  PrintStream getErrorStream() {
    return errorStream;
  }

  private static Path getSourcePrefixPath(ImmutableSet<Path> sources, ImmutableSet<Path> modules) {
    Path prefix = Paths.getCommonPrefix(Iterables.concat(sources, modules));
    if (sources.contains(prefix) || modules.contains(prefix)) {
      prefix = prefix.getParent();
    }
    return prefix;
  }

  private static Path getModulePreixPath(
      Optional<Path> userSupplierPath, ImmutableSet<Path> modules) {
    Path path;
    if (userSupplierPath.isPresent()) {
      path = userSupplierPath.get();
      checkArgument(Files.isDirectory(path), "Module prefix must be a directory: %s", path);
      for (Path module : modules) {
        checkArgument(module.startsWith(path),
            "Module prefix <%s> is not an ancestor of module %s", path, module);
      }
    } else {
      path = Paths.getCommonPrefix(modules);
      if (modules.contains(path) && path.getParent() != null) {
        path = path.getParent();
      }
    }

    // Always display at least one parent directory, if possible.
    for (Path module : modules) {
      if (path.equals(module.getParent())) {
        return Objects.firstNonNull(path.getParent(), path);
      }
    }

    return path;
  }

  /**
   * Loads a new runtime configuration from the provided input stream.
   */
  static Config load(InputStream stream) {
    ConfigSpec spec = ConfigSpec.load(stream);
    checkArgument(spec.output != null, "Output not specified");
    checkArgument(!Files.exists(spec.output) || Files.isDirectory(spec.output),
        "Output path exists, but is not a directory: %s", spec.output);

    @SuppressWarnings("unchecked")
    Predicate<Path> filter = Predicates.and(
        notExcluded(resolve(spec.excludes)),
        notHidden());

    Iterable<Path> filteredSources = from(resolve(spec.sources)).filter(filter);
    Iterable<Path> filteredModules = from(resolve(spec.modules)).filter(filter);

    if (spec.closureLibraryDir.isPresent()) {
      ImmutableSet<Path> depsFiles = ImmutableSet.<Path>builder()
          .add(spec.closureLibraryDir.get().resolve("deps.js"))
          .addAll(spec.closureDepsFile)
          .build();

      try {
        filteredSources = processClosureSources(
            filteredSources, depsFiles, spec.closureLibraryDir.get());
      } catch (IOException | SortedDependencies.CircularDependencyException e) {
        throw new RuntimeException(e);
      }
    }

    return new Config(
        ImmutableSet.copyOf(filteredSources),
        ImmutableSet.copyOf(filteredModules),
        ImmutableSet.copyOf(resolve(spec.externs)),
        spec.output,
        spec.license,
        spec.readme,
        spec.stripModulePrefix,
        spec.strict,
        spec.language,
        System.out,
        System.err);
  }

  private static Iterable<Path> resolve(Iterable<PathSpec> specs) {
    Iterable<List<Path>> paths = from(specs)
        .transform(new Function<PathSpec, List<Path>>() {
          @Override
          public List<Path> apply(PathSpec input) {
            try {
              return input.resolve();
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        });
    return Iterables.concat(paths);
  }

  private static ImmutableSet<Path> processClosureSources(
      Iterable<Path> sources, ImmutableSet<Path> deps,
      Path closureBase) throws SortedDependencies.CircularDependencyException, IOException {

    Collection<SourceFile> depsFiles = newLinkedList(transform(deps, toSourceFile()));
    Collection<SourceFile> sourceFiles = newLinkedList(transform(sources, toSourceFile()));

    ErrorManager errorManager = new PrintStreamErrorManager(System.err);

    DepsGenerator generator = new DepsGenerator(
        depsFiles,
        sourceFiles,
        DepsGenerator.InclusionStrategy.ALWAYS,
        closureBase.toAbsolutePath().toString(),
        errorManager);

    String rawDeps = generator.computeDependencyCalls();
    errorManager.generateReport();
    if (rawDeps == null) {
      throw new RuntimeException("Encountered Closure dependency conflicts");
    }

    List<DependencyInfo> allDeps = new DepsFileParser(errorManager)
        .parseFile("*generated-deps*", rawDeps);

    List<DependencyInfo> sourceDeps =
        from(allDeps)
        .filter(isInSources(sources, closureBase))
        .toList();

    List<DependencyInfo> sortedDeps = new SortedDependencies<>(allDeps)
        .getDependenciesOf(sourceDeps, true);

    return ImmutableSet.<Path>builder()
        // Always include Closure's base.js first.
        .add(closureBase.resolve("base.js"))
        .addAll(transform(sortedDeps, toPath(closureBase)))
        .build();
  }

  private static Predicate<DependencyInfo> isInSources(
      final Iterable<Path> sources, Path closureBaseDir) {
    final Function<DependencyInfo, Path> pathTransform = toPath(closureBaseDir);
    final ImmutableSet<Path> sourcesSet = ImmutableSet.copyOf(sources);
    return new Predicate<DependencyInfo>() {
      @Override
      public boolean apply(DependencyInfo input) {
        return sourcesSet.contains(pathTransform.apply(input));
      }
    };
  }

  private static Function<DependencyInfo, Path> toPath(final Path closureBaseDir) {
    return new Function<DependencyInfo, Path>() {
      @Override
      public Path apply(DependencyInfo input) {
        return closureBaseDir.resolve(input.getPathRelativeToClosureBase())
            .normalize()
            .toAbsolutePath();
      }
    };
  }

  private static Function<Path, SourceFile> toSourceFile() {
    return new Function<Path, SourceFile>() {
      @Override
      public SourceFile apply(Path input) {
        return SourceFile.fromFile(input.toAbsolutePath().toFile());
      }
    };
  }

  private static Predicate<Path> notExcluded(final Iterable<Path> excludes) {
    return new Predicate<Path>() {
      @Override
      public boolean apply(Path input) {
        for (Path exclude : excludes) {
          if (input.equals(exclude)) {
            return false;
          }
        }
        return true;
      }
    };
  }

  private static Predicate<Path> notHidden() {
    return new Predicate<Path>() {
      @Override
      public boolean apply(Path input) {
        try {
          return !Files.isHidden(input);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  @VisibleForTesting
  static class PathSpec {
    private final Path baseDir;
    private final String spec;

    PathSpec(String spec) {
      this(FileSystems.getDefault().getPath(""), spec);
    }

    @VisibleForTesting
    PathSpec(Path baseDir, String spec) {
      this.baseDir = baseDir;
      this.spec = spec;
    }

    List<Path> resolve() throws IOException {
      Path path = baseDir.resolve(spec);
      if (Files.isDirectory(path)) {
        return collectFiles(path, "**.js");
      }

      if (Files.exists(path)) {
        return ImmutableList.of(path);
      }

      return collectFiles(baseDir, spec);
    }

    List<Path> collectFiles(final Path baseDir, String glob) throws IOException {
      final PathMatcher matcher = baseDir.getFileSystem().getPathMatcher("glob:" + glob);
      final ImmutableList.Builder<Path> files = ImmutableList.builder();
      Files.walkFileTree(baseDir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
          if (matcher.matches(baseDir.relativize(file))) {
            files.add(file);
          }
          return FileVisitResult.CONTINUE;
        }
      });
      return files.build();
    }
  }

  static String getOptionsText() {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    pw.println("**Configuration Options**");
    pw.println();

    for (Field field : ConfigSpec.class.getDeclaredFields()) {
      Description description = field.getAnnotation(Description.class);
      if (description != null) {
        String str = String.format(" * `%s` ", field.getName()) + description.value().trim();
        boolean isFirst = true;
        for (String line : Splitter.on("\n").split(str)) {
          if (isFirst) {
            printLine(pw, line);
            isFirst = false;
          } else {
            printLine(pw, "   " + line);
          }
        }
      }
      pw.println();
    }
    return sw.toString();
  }

  private static void printLine(PrintWriter pw, String line) {
    if (line.length() <= 79) {
      pw.println(line);
    } else {
      int index = 79;
      while (line.charAt(index) != ' ') {
        index -= 1;
      }
      while (line.charAt(index) == '.'
          && index + 1 < line.length()
          && line.charAt(index + 1) != ' ') {
        index -= 1;
      }
      pw.println(line.substring(0, index));
      printLine(pw, "   " + line.substring(index));
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  private @interface Description {
    String value();
  }

  private static class ConfigSpec {

    @Description("Path to the directory to write all generated documentation to. This field is" +
        " required.")
    private final Path output = null;

    @Description("Path to the base directory of the Closure library (which must contain base.js" +
        " and depsjs). When this option is specified, Closure's deps.js and all of the files" +
        " specified by `closureDepsFile` will be parsed for calls to `goog.addDependency`. The" +
        " resulting map will be used to automatically expand the set of `sources` any time a" +
        " symbol is goog.require'd with the ile that goog.provides that symbol, along with all" +
        " of its transitive dependencies.\n" +
        "\n" +
        " For example, suppose you have one source file, `foo.js`:\n" +
        "\n" +
        "     goog.require('goog.array');\n" +
        "     // ...\n" +
        "\n" +
        " and your configuration includes:\n" +
        "\n" +
        "     \"sources\": [\"foo.js\"],\n" +
        "     \"closureLibraryDir\": \"closure/goog\"\n" +
        "\n" +
        " due to the dependencies of goog.array declared in closure/goog/deps.js, this is" +
        " equivalent to the following configuration:\n" +
        "\n" +
        "     \"sources\": [\n" +
        "         \"closure/goog/base.js\",\n" +
        "         \"closure/goog/debug/error.js\",\n" +
        "         \"closure/goog/string/string.js\",\n" +
        "         \"closure/goog/asserts/asserts.js\",\n" +
        "         \"closure/goog/array/array.js\",\n" +
        "         \"foo.js\"\n" +
        "     ]\n" +
        "\n" +
        " Notice specifying `closureLibraryDir` instructs Dossier to sort the input files so a" +
        " a file that goog.provides symbol X comes before any file that goog.requires X.")
    private final Optional<Path> closureLibraryDir = Optional.absent();

    @Description("Path to a file to parse for calls to `goog.addDependency`. This option " +
        "requires  also setting `closureLibraryDir`.")
    private final List<Path> closureDepsFile = ImmutableList.of();

    @Description("A list of .js files to extract API documentation from. If a glob pattern " +
        "is specified, every .js file under the current working directory matching that pattern" +
        " will be included. Specifying the path to a directory, `foo`, is the same as using " +
        "the glob pattern `foo/**.js`. The set of paths specified by this option *must* be " +
        "disjoint from those specified by `modules`.")
    private final List<PathSpec> sources = ImmutableList.of();

    @Description("A list of .js files to extract API documentation from. Each file will be " +
        "processed as a CommonJS module, with only its exported API included in the generated" +
        " output. If a glob pattern is specified, every .js file under the current directory " +
        "matching that pattern will be included. Specifying the path to a directory, `foo`, is" +
        " the same as the glob pattern `foo/**.js`. The set of paths specified by this option " +
        "*mut* be disjoint from those specified by `sources`.")
    private final List<PathSpec> modules = ImmutableList.of();

    @Description("A prefix to strip from every module's path when generating documentation." +
        " The specified path must be a directory that is an ancestor of every file specified " +
        "in `modules`. Note: if this option is omitted, the closest common ancestor for all " +
        "module files will be selected as the default.")
    private final Optional<Path> stripModulePrefix = Optional.absent();

    @Description("A list of .js files to exclude from processing. If a directory is specified," +
        " all of the .js files under that directory will be excluded. A glob pattern may also" +
        " be specified to exclude all of the paths under the current working directory that " +
        "match  the provided pattern.")
    private final List<PathSpec> excludes = ImmutableList.of();

    @Description("A list of .js files to include as an extern file for the Closure compiler. " +
        "These  files are used to satisfy references to external types, but are excluded when " +
        "generating  API documentation.")
    private final List<PathSpec> externs = ImmutableList.of();

    @Description("Path to a license file to include with the generated documentation. If " +
        "specified, a link to the license will be included on every page.")
    private final Optional<Path> license = Optional.absent();

    @Description("Path to a README file to include as the main landing page for the generated " +
        "documentation. This file should use markdown syntax.")
    private final Optional<Path> readme = Optional.absent();

    @Description("Whether to run with all type checking flags enabled.")
    private final boolean strict = false;

    @Description("Specifies which version EcmaScript the input sources conform to. Defaults " +
        "to ES5.")
    private final Language language = Language.ES5;

    static ConfigSpec load(InputStream stream) {
      Gson gson = new GsonBuilder()
          .registerTypeAdapter(Path.class, new PathDeserializer())
          .registerTypeAdapter(PathSpec.class, new PathSpecDeserializer())
          .registerTypeAdapter(
              new TypeToken<Optional<Path>>(){}.getType(),
              new OptionalDeserializer<>(Path.class))
          .create();

      return gson.fromJson(
          new InputStreamReader(stream, StandardCharsets.UTF_8),
          ConfigSpec.class);
    }
  }

  static enum Language {
    ES3("ECMASCRIPT3"),
    ES5("ECMASCRIPT5"),
    ES5_STRICT("ECHMASCRIPT5_STRICT");

    private final String fullName;

    Language(String fullName) {
      this.fullName = fullName;
    }

    public String getName() {
      return fullName;
    }
  }

  private static class OptionalDeserializer<T> implements JsonDeserializer<Optional<T>> {

    private final Class<T> componentType;

    private OptionalDeserializer(Class<T> componentType) {
      this.componentType = componentType;
    }

    @Override
    public Optional<T> deserialize(
        JsonElement jsonElement, Type type, JsonDeserializationContext context)
        throws JsonParseException {
      if (jsonElement.isJsonNull()) {
        return Optional.absent();
      }
      T value = context.deserialize(jsonElement, componentType);
      return Optional.fromNullable(value);
    }
  }

  private static class PathDeserializer implements JsonDeserializer<Path> {

    @Override
    public Path deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
        throws JsonParseException {
      return FileSystems.getDefault().getPath(jsonElement.getAsString())
          .toAbsolutePath()
          .normalize();
    }
  }

  private static class PathSpecDeserializer implements JsonDeserializer<PathSpec> {

    @Override
    public PathSpec deserialize(
        JsonElement jsonElement, Type type, JsonDeserializationContext context)
        throws JsonParseException {
      return new PathSpec(jsonElement.getAsString());
    }
  }

  public static void main(String[] args) {
    System.err.println(getOptionsText());
  }
}
