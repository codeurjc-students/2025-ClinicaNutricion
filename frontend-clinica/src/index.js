import React from "react";
import ReactDOM from "react-dom";
import App from "./App";
import { Amplify } from "aws-amplify";
import awsconfig from "./aws-exports";
import 'bootstrap/dist/css/bootstrap.min.css'; // Importar estilos de Bootstrap

Amplify.configure(awsconfig);

ReactDOM.render(<App />, document.getElementById("root"));
