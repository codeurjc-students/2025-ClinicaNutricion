const clientId = "38902ociv96ik2ih3p9446ela2";
const cognitoDomain = "https://eu-west-3akiycc7tp.auth.eu-west-3.amazoncognito.com";
const logoutUri = "http://localhost:3000/login"; // URL a la que redirigir tras logout

export const login = () => {
    window.location.href = "http://localhost:3000/login";
};

// Función para eliminar las cookies de sesión
const clearSessionCookies = () => {
    document.cookie.split(";").forEach((cookie) => {
        document.cookie = cookie
            .replace(/^ +/, "")
            .replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
    });
};

export const logout = () => {
    clearSessionCookies(); // Asegura que las cookies de sesión sean eliminadas
    window.location.href = `${cognitoDomain}/logout?client_id=${clientId}&logout_uri=${encodeURIComponent(logoutUri)}`;
};