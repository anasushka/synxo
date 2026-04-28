(function () {
	const AUTH_STORAGE_KEY = "synxo-demo-auth";

	function loadAuth() {
		try {
			return JSON.parse(localStorage.getItem(AUTH_STORAGE_KEY)) || { email: "", password: "" };
		} catch (error) {
			return { email: "", password: "" };
		}
	}

	function saveAuth(auth) {
		localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth));
	}

	function clearAuth() {
		localStorage.removeItem(AUTH_STORAGE_KEY);
	}

	function hasAuth(auth = loadAuth()) {
		return Boolean(auth.email && auth.password);
	}

	function toBase64(value) {
		const bytes = new TextEncoder().encode(value);
		let binary = "";
		bytes.forEach(byte => {
			binary += String.fromCharCode(byte);
		});
		return btoa(binary);
	}

	async function apiRequest(path, options = {}) {
		const {
			method = "GET",
			body,
			auth = loadAuth(),
			bodyType = "json"
		} = options;

		const headers = {};
		if (auth && auth.email && auth.password) {
			headers.Authorization = "Basic " + toBase64(auth.email + ":" + auth.password);
		}

		let payload = body;
		if (body !== undefined && bodyType === "json") {
			headers["Content-Type"] = "application/json";
			payload = JSON.stringify(body);
		}

		const response = await fetch(path, {
			method,
			headers,
			body: payload
		});

		const raw = await response.text();
		const parsed = tryParseJson(raw);

		if (!response.ok) {
			throw new Error(extractErrorMessage(response.status, parsed, raw));
		}

		return parsed;
	}

	function tryParseJson(value) {
		if (!value) {
			return null;
		}
		try {
			return JSON.parse(value);
		} catch (error) {
			return null;
		}
	}

	function extractErrorMessage(status, payload, raw) {
		if (payload && payload.validationErrors) {
			return Object.values(payload.validationErrors).join(" | ");
		}
		if (payload && payload.message) {
			return payload.message;
		}
		if (payload && payload.error) {
			return payload.error;
		}
		if (status === 401) {
			return "Проверь email и пароль. Сервер не принял авторизацию.";
		}
		return raw || ("Ошибка запроса: HTTP " + status);
	}

	function escapeHtml(value) {
		return String(value)
			.replaceAll("&", "&amp;")
			.replaceAll("<", "&lt;")
			.replaceAll(">", "&gt;")
			.replaceAll('"', "&quot;")
			.replaceAll("'", "&#39;");
	}

	function formatDate(value) {
		if (!value) {
			return "Пока без сообщений";
		}
		return new Date(value).toLocaleString("ru-RU", {
			day: "2-digit",
			month: "short",
			hour: "2-digit",
			minute: "2-digit"
		});
	}

	function initialsFrom(value) {
		return value
			.split(/\s+/)
			.filter(Boolean)
			.slice(0, 2)
			.map(word => word[0].toUpperCase())
			.join("") || "SX";
	}

	window.SynxoShared = {
		apiRequest,
		clearAuth,
		escapeHtml,
		formatDate,
		hasAuth,
		initialsFrom,
		loadAuth,
		saveAuth
	};
})();
