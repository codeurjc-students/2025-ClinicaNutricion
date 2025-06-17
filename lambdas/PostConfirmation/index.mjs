import {
  CognitoIdentityProviderClient,
  AdminAddUserToGroupCommand
} from "@aws-sdk/client-cognito-identity-provider";
import {
  LambdaClient,
  InvokeCommand
} from "@aws-sdk/client-lambda";
import {
  EventBridgeClient,
  PutEventsCommand
} from "@aws-sdk/client-eventbridge";

const cognito = new CognitoIdentityProviderClient({ region: "eu-west-3" });
const lambda = new LambdaClient({ region: "eu-west-3" });
const eventbridge = new EventBridgeClient({ region: "eu-west-3" });

export const handler = async (event) => {
  console.log("Lambda A inicia con el evento:", JSON.stringify(event));

  const {
    userName,
    userPoolId,
    request: { userAttributes }
  } = event;
  const groupName = "patient";

  // Añadir al grupo Cognito
  try {
    const cmd = new AdminAddUserToGroupCommand({
      UserPoolId: userPoolId,
      Username: userName,
      GroupName: groupName
    });
    await cognito.send(cmd);
    console.log(`Usuario ${userName} agregado al grupo ${groupName}`);
  } catch (err) {
    console.error("Error asignando grupo en Cognito:", err);

    // Publicar evento de compensación
    await eventbridge.send(new PutEventsCommand({
      Entries: [{
        Source: "clinicanutricion.compensation",
        DetailType: "PostConfirmationFailed",
        Detail: JSON.stringify({ userName, userPoolId }),
        EventBusName: "default"
      }]
    }));

    return event;
  }

  // Invocamos Lambda B solo si la lambda anterior ha tenido exito
  const payload = JSON.stringify({ userName, userAttributes });

  try {
    const invokeCmd = new InvokeCommand({
      FunctionName: "OnboardPatientFunction",
      InvocationType: "Event",  // asíncrono
      Payload: Buffer.from(payload)
    });
    await lambda.send(invokeCmd);
    console.log("Lambda B invocada correctamente para persistir en RDS");
  } catch (err) {
    console.error("Error invocando Lambda B:", err);

    // Publicar evento de compensación
    await eventbridge.send(new PutEventsCommand({
      Entries: [{
        Source: "clinicanutricion.compensation",
        DetailType: "PostConfirmationFailed",
        Detail: JSON.stringify({ userName, userPoolId }),
        EventBusName: "default"
      }]
    }));
  }

  console.log("Lambda A completada sin bloqueos ");
  return event;
};
