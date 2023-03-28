# CHANGELOG

## Version 0.2.0 (2023-03-28)
 - **BREAKING CHANGE:**
     - `Digest.compress` function was changed to also include an offset.
       This drastically improves performance by mitigating excessive/unnecessary
       array copying.

## Version 0.1.1 (2023-03-06)
 - Fix `Digest.update` miscalculation when `offset` parameter is provided

## Version 0.1.0 (2023-03-04)
 - Initial Release
