# Third-party notices

This file records attribution and license terms for data and other third-party
material used by World Countries Information beyond what is covered by the
project's Apache 2.0 source license.

## Country data — mledoze/countries (ODbL 1.0)

Country facts shown in the app (names, capitals, regions, populations, codes,
and related fields) are fetched at runtime from the
[mledoze/countries](https://github.com/mledoze/countries) dataset:

- Source: `https://github.com/mledoze/countries`
- Data file: `https://raw.githubusercontent.com/mledoze/countries/master/countries.json`
- License: [Open Database License (ODbL) v1.0](https://opendatacommons.org/licenses/odbl/1.0/)

The app may cache this data locally (Room) for offline use. Cached copies remain
subject to the ODbL terms for the underlying database contents.

**Flag emoji / flag assets in the mledoze dataset are not licensed under ODbL.**
See the mledoze/countries repository for flag licensing details.

## Population — World Bank (CC BY 4.0)

Population figures are fetched at runtime from the World Bank Indicators API,
indicator `SP.POP.TOTL` ("Population, total"), most recent value per economy:

- Endpoint: `https://api.worldbank.org/v2/country/all/indicator/SP.POP.TOTL?format=json&mrv=1`
- License: [Creative Commons Attribution 4.0](https://datacatalog.worldbank.org/public-licenses#cc-by)
- Attribution: World Bank, World Development Indicators

## Map data — OpenStreetMap (ODbL 1.0)

Map tiles and geodata on the country details screen use OpenStreetMap. See
[OpenStreetMap copyright](https://www.openstreetmap.org/copyright). Map
attribution is shown in-app where required.

## Typeface — Fraunces (SIL Open Font License 1.1)

Headings use Fraunces, bundled as
`core/designsystem/src/main/res/font/fraunces_semibold.ttf`
(`Fraunces72pt-SemiBold.ttf` from the upstream repository).

- Copyright 2018 The Fraunces Project Authors
  (`https://github.com/undercasetype/Fraunces`)
- License: [SIL Open Font License, Version 1.1](https://openfontlicense.org)

The font is used unmodified. Under the OFL it may be bundled with this software
but may not be sold on its own.
