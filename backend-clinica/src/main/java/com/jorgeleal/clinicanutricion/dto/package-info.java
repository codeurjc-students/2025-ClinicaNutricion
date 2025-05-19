/**
  Suprime las advertencias de FindBugs EI_EXPOSE_REP y EI_EXPOSE_REP2,
  que indican exposición de la representación interna (arrays u objetos).
**/
@SuppressFBWarnings(
  value = {"EI_EXPOSE_REP","EI_EXPOSE_REP2"},
  justification = "DTOs exponen referencias para mapeo, no para mutación externa"
)
package com.jorgeleal.clinicanutricion.dto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;