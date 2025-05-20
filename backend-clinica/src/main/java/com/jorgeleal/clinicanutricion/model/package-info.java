/**
  Suprime las advertencias EI_EXPOSE_REP y EI_EXPOSE_REP2 de FindBugs,
  que alertan sobre la exposición de la representación interna de objetos.
**/

@SuppressFBWarnings(
  value = {"EI_EXPOSE_REP","EI_EXPOSE_REP2"},
  justification = "Las relaciones de entidad JPA se gestionan internamente y no se exponen para mutación fuera de la capa de persistencia"
)
package com.jorgeleal.clinicanutricion.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
