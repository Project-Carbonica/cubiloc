## [1.0.1](https://github.com/Project-Carbonica/cubiloc/compare/v1.0.0...v1.0.1) (2025-12-23)


### Bug Fixes

* add support for SingleMessageResult and ListMessageResult in placeholder value resolution ([50b87de](https://github.com/Project-Carbonica/cubiloc/commit/50b87dec5e88ec88e5c4ff14fd3e3e88ce32b28c))

# 1.0.0 (2025-12-22)


### Bug Fixes

* add missing Nyx mark command and resume flags for proper release workflow ([5b3982c](https://github.com/Project-Carbonica/cubiloc/commit/5b3982c778937903109e575199a6a27ede2fe04b))
* add Nexus credentials to CI and release workflows ([3d71c3c](https://github.com/Project-Carbonica/cubiloc/commit/3d71c3c03d681d95984750c5718df9a9212145fc))
* Add post-processor to MiniMessage for default italic decoration ([e6ae95b](https://github.com/Project-Carbonica/cubiloc/commit/e6ae95b2e0c228ab5eb2748cbc501b584b617884))
* clarify commit message conventions and update Nyx infer step identifiers ([7443e16](https://github.com/Project-Carbonica/cubiloc/commit/7443e167afcedbdf57acf4a7e918b19f886261f5))
* clean state file on infer and enable gitTagForce ([3d011f7](https://github.com/Project-Carbonica/cubiloc/commit/3d011f7582599fc7891db3a6705ea67136921806))
* disable gitCommit and add Git remote credentials for Nyx push ([a0de646](https://github.com/Project-Carbonica/cubiloc/commit/a0de64638405d96a79890aa73bc170b6bb6b1adf))
* disable Nyx gitPush and use workflow Git CLI for pushing tags ([a3478a0](https://github.com/Project-Carbonica/cubiloc/commit/a3478a059d4487f1870a8dffe54f70ad56fe8e8d))
* enable gitCommit and gitPush in Nyx configuration for proper release workflow ([599bd61](https://github.com/Project-Carbonica/cubiloc/commit/599bd61b83aaedbc287e1d98d2d737f68541bccb))
* enhance release workflow by updating Nyx infer and adding version handling ([6b89634](https://github.com/Project-Carbonica/cubiloc/commit/6b89634ba5df2778b3646f38b8e9233b268f8602))
* explicitly enable conventionalCommits convention ([ef7608e](https://github.com/Project-Carbonica/cubiloc/commit/ef7608eaa2cf225138ab8c6b3c4146d9c8ba9a0c))
* publish only to Nexus repository instead of all repositories ([1c5d06e](https://github.com/Project-Carbonica/cubiloc/commit/1c5d06e1f5b5fabcba030e7cad013dd67c83e6ea))
* remove branch matching restriction in Nyx configuration ([adbcd96](https://github.com/Project-Carbonica/cubiloc/commit/adbcd96f1c2870e98f7d2a3ba4f30a82f23d9e0b))
* remove Nyx state file from .gitignore ([5220055](https://github.com/Project-Carbonica/cubiloc/commit/5220055c9c48663a1dce5f06ee7272138870bac7))
* restore Git remote URL token configuration for JGit authentication ([9dd200c](https://github.com/Project-Carbonica/cubiloc/commit/9dd200cd1f63275195ed91c8f9ca7c159ab3e479))
* switch to conventionalCommits preset for proper semantic versioning ([d29d71d](https://github.com/Project-Carbonica/cubiloc/commit/d29d71dad8a0c55118c8b0a0ec984d4a9de683f9))
* trigger version bump to 0.4.1 ([33202b2](https://github.com/Project-Carbonica/cubiloc/commit/33202b291cf400fc2c459f9c8fce362d1efc491a))
* update Nyx configuration for improved release workflow and Git integration ([b378eff](https://github.com/Project-Carbonica/cubiloc/commit/b378eff994943d7eae418ebdfe937fc193f6841e))


### Features

* Add Dependency Injection support for Cubiloc I18n using Dagger and Guice ([055770c](https://github.com/Project-Carbonica/cubiloc/commit/055770cfd5f178f689e56b72bcc3e2423296fc8e))
* Enhance I18n with locale provider support and refactor DI integration ([f1801fa](https://github.com/Project-Carbonica/cubiloc/commit/f1801fa1894c8c126d5ab9ab4a61a1993f87362c))
* enhance Nyx infer step with state file handling and default values ([c021e8b](https://github.com/Project-Carbonica/cubiloc/commit/c021e8b51b6151718d1905d004a4d2083891011f))
* enhance Nyx setup and infer steps in release workflow ([2ed62d1](https://github.com/Project-Carbonica/cubiloc/commit/2ed62d14139a56cf2cd623c35b430449186a058f))
* initialize project structure with Gradle setup and theme configurations ([0ad1c2b](https://github.com/Project-Carbonica/cubiloc/commit/0ad1c2bd71403674d60023aff585083fc692d7a9))
* introduce context system for zero-boilerplate message retrieval and add Okaeri transformers for message result types ([c99e703](https://github.com/Project-Carbonica/cubiloc/commit/c99e703e1360057a7e011e29621198e58eff3ebd))
* Refactor I18n integration to use LocaleProvider, removing I18nProvider dependency ([ad83b03](https://github.com/Project-Carbonica/cubiloc/commit/ad83b033c4bba2bde402c06422a33b24cdf155dc))
* Remove old message configuration files and add new CI and release workflows ([4b1e2b3](https://github.com/Project-Carbonica/cubiloc/commit/4b1e2b34aafd89dc5216e13b676e33b46902848c))

# Changelog

## 0.4.1 (2025-11-29)

### fix

* [1c5d0] fix: publish only to Nexus repository instead of all repositories (deichor)
