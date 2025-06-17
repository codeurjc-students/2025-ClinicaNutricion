import AWS from "aws-sdk";
const cognito = new AWS.CognitoIdentityServiceProvider();

export const handler = async (event) => {
  console.log("Evento de compensación recibido:", JSON.stringify(event));
  const { userName } = event.detail;

  try {
    await cognito.adminDeleteUser({
      UserPoolId: process.env.COGNITO_USER_POOL_ID,
      Username: userName
    }).promise();
    console.log(`Usuario ${userName} eliminado de Cognito`);
  } catch (err) {
    console.error("Error borrando usuario en Cognito:", err);
    throw err;
  }
};
