# ETDP
Estimated Time of Departure Prediction


## Build
To be able to build this version, you need to do the following:

1. Create a file `secrets.properties` in the root directory of the project.

2. Paste `DM_API_KEY={YOUR_API_KEY}` in the file.

3. Replace `{YOUR_API_KEY}` with your Google API key (get key from [Developer Console](https://console.developers.google.com/apis/credentials) or
[Documentation  page](https://developers.google.com/maps/documentation/distance-matrix/get-api-key#get-an-api-key)).

4. Paste `W_API_KEY={YOUR_API_KEY}` in the file in a new line.

5. Replace `{YOUR_API_KEY}` with your OpenWeatherMap API key (get key from [OpenWeatherMap API keys Page](https://home.openweathermap.org/api_keys)).

## Contribute
To contribute to this project, please follow:
#### Commit Message Format
Each commit message consists of a header, a body and a footer. The header has a special format that includes a type, a scope and a subject:

    <type>(<scope>): <subject>
    <BLANK LINE>
    <body>
    <BLANK LINE>
    <footer>
The header is mandatory and the scope of the header is optional.

Any line of the commit message cannot be longer 100 characters! This allows the message to be easier to read on GitHub as well as in various git tools.
