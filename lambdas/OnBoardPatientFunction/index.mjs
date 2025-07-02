import { EventBridgeClient, PutEventsCommand } from "@aws-sdk/client-eventbridge";
import mysql from "mysql2/promise";

const eventbridge = new EventBridgeClient({ region: process.env.AWS_REGION });

const pool = mysql.createPool({
  host: process.env.RDS_HOST,
  user: process.env.RDS_USER,
  password: process.env.RDS_PASSWORD,
  database: process.env.RDS_DATABASE,
  waitForConnections: true,
  connectionLimit: 5,
  queueLimit: 0
});

export const handler = async (event) => {
  console.log("Lambda B inicia con el evento:", JSON.stringify(event));

  // Parseamos payload
  let payload;
  if (event.userName && event.userAttributes) {
    payload = event;
  } else if (event.Records?.[0]?.body) {
    payload = JSON.parse(event.Records[0].body);
  } else {
    console.error("Payload inesperado:", event);
    return;
  }

  const { userName, userAttributes } = payload;

  // Obtener conexión del pool
  let conn;
  try {
    conn = await pool.getConnection();
    console.log("Conexión a RDS establecida (del pool)");
  } catch (err) {
    console.error("No se pudo obtener conexión del pool:", err);
    await eventbridge.send(new PutEventsCommand({
      Entries: [{
        Source: "clinicanutricion.compensation",
        DetailType: "RDSInsertionFailed",
        Detail: JSON.stringify({ userName }),
        EventBusName: "default"
      }]
    }));
    return;
  }

  // Insertar en tabla user
  let userId;
  try {
    const [result] = await conn.execute(
      `INSERT INTO user
         (cognito_id, name, surname, mail, phone, birth_date, gender, user_type)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        userName,
        userAttributes.name ?? null,
        userAttributes.family_name ?? null,
        userAttributes.email ?? null,
        userAttributes.phone_number ?? null,
        userAttributes.birthdate ?? null,
        userAttributes.gender ?? null,
        "PATIENT"
      ]
    );
    userId = result.insertId;
    console.log(`Usuario insertado con insertId=${userId}`);
  } catch (err) {
    console.error("Error al insertar usuario:", err);
    await eventbridge.send(new PutEventsCommand({
      Entries: [{
        Source: "clinicanutricion.compensation",
        DetailType: "RDSInsertionFailed",
        Detail: JSON.stringify({ userName }),
        EventBusName: "default"
      }]
    }));
    await conn.release();
    return;
  }

  // Insertar en tabla patient
  try {
    await conn.execute(
      `INSERT INTO patient (id_user, active) VALUES (?, ?)`,
      [userId, true]
    );
    console.log("Paciente vinculado con id_user=", userId);
  } catch (err) {
    console.error("Error al insertar paciente:", err);
    await eventbridge.send(new PutEventsCommand({
      Entries: [{
        Source: "clinicanutricion.compensation",
        DetailType: "RDSInsertionFailed",
        Detail: JSON.stringify({ userName }),
        EventBusName: "default"
      }]
    }));
    await conn.release();
    return;
  }

  // Liberar conexión
  try {
    await conn.release();
    console.log("Lambda B completada");
  } catch (err) {
    console.warn("Error liberando conexión:", err);
  }
};