const LandingShared = window.SynxoShared;

initLandingPage();

async function initLandingPage() {
	const auth = LandingShared.loadAuth();
	if (!LandingShared.hasAuth(auth)) {
		return;
	}

	try {
		await LandingShared.apiRequest("/api/auth/me", { auth });
		window.location.replace("/app.html");
	} catch (error) {
		if (LandingShared.isUnauthorizedError(error)) {
			LandingShared.clearAuth();
		}
	}
}
