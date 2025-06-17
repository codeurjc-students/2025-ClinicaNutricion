export const handler = async (event) => {
  const { triggerSource, request: { userAttributes } } = event;

  if (triggerSource === 'PreSignUp_SignUp') {
    const errors = [];

    const name = (userAttributes.name || '').trim();
    const surname = (userAttributes.family_name || '').trim();
    const birthdate = userAttributes.birthdate || '';
    const gender = (userAttributes.gender || '').trim();

    const nameRegex    = /^[A-Za-zÁÉÍÓÚáéíóúÑñ\- ]{2,50}$/;
    const surnameRegex = /^[A-Za-zÁÉÍÓÚáéíóúÑñ\- ]{2,50}$/;
    
    if (!nameRegex.test(name)) {
      errors.push('El nombre debe tener entre 2 y 50 caracteres y solo letras, espacios y guiones');
    }
    
    if (!surnameRegex.test(surname)) {
      errors.push('El apellido debe tener entre 2 y 50 caracteres y solo letras, espacios y guiones');
    }

    if (!/^\d{4}-\d{2}-\d{2}$/.test(birthdate) 
        || new Date(birthdate) >= new Date()) {
      errors.push('. La fecha de nacimiento debe ser anterior a la fecha actual');
    }

    const allowedGenders = ['Masculino', 'Femenino', 'Otro'];
    if (!allowedGenders.some(g => g.toLowerCase() === gender.toLowerCase())) {
      errors.push('. El género debe ser: Masculino, Femenino, Otro');
    }

    if (errors.length) {
      console.error('PreSignUpValidation falló:', errors);
      throw new Error(errors.join(' '));
    }
  }

  return event;
};