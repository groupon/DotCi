## Plugins

These are common to the built-in Build Types; while its
suggested that any new Build Types utilize them, it is not enforced
through the DotCi Plugin Framework. Any Build Type that respects the
`plugins` keys can use these:

* [artifacts](#-artifacts)
* [checkstyle](#-checkstyle)
* [cobertura](#-cobertura)
* [downstream_job](#-downstream-job)
* [findbugs](#-findbugs)
* [output_files](#-output-files)
* [test_output](#-test-output)


### `artifacts`
```yaml
---
plugins:
  artifacts:
    - file1.txt
    - dist/file2.*
```
This plugin configures Jenkin's default artifact archiver; it is
essentially an array of the comma separated matchers to archive in the
old settings. __NOTE: the artifacts must exist, there is no exposure of
the Advanced options to ignore missing artifacts.__

See [ant fileset](http://ant.apache.org/manual/Types/fileset.html) for
matcher specifications.

_NOTE: Configure your project's Build Environment: "Delete workspace before build starts" to avoid accumulative artifacts._


### `checkstyle`
```yaml
---
plugins:
  checkstyle
```
**FIXME: Describe file at `target/checkstyle-result.xml` or move out
since not in base install**


### `cobertura`
```yaml
---
plugins:
  cobertura
```
**FIXME: Describe file at `target/site/cobertura/coverage.xml` or move
out since not in base install**


### `downstream_job`
```yaml
---
plugins:
  downstream_job:
    on_result: <SUCCESS|UNSTABLE|FAILURE|NOT_BUILT|ABORTED> # From hudson.model.Result static types
    foobar:
      k1: v1
      k2: v2
```
This triggers the job by the name of `foobar` when the current job's
result matches the value of `on_result`, which comes from
[`hudson.model.Result`](https://github.com/kohsuke/hudson/blob/7a64e030a38561c98954c4c51c4438c97469dfd6/core/src/main/java/hudson/model/Result.java).
The hash that is the key `foobar`'s value are passed into the downstream
job as its parameterized values.


### `findbugs`
```yaml
---
plugins:
  findbugs
```
**FIXME: Describe file at `target/findbugs.xml` or move out since not in
base install**


### `output_files`
```yaml
---
plugins:
  output_files:
    - foo.txt
    - bar.txt
```
This allows copying specific output files for further plugin process.


### `test_output`
```yaml
---
plugins:
  test_output:
    format: tap | junit
```
**FIXME: Describe or move out since not in base install**
