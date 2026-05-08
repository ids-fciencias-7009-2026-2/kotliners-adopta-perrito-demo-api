(() => {
    const state = {
        animalId: new URLSearchParams(window.location.search).get("id"),
        token: findToken()
    };

    const nodes = {
        statusText: document.getElementById("statusText"),
        backButton: document.getElementById("backButton"),
        ownerBadge: document.getElementById("ownerBadge"),
        ownerActions: document.getElementById("ownerActions"),
        deleteButton: document.getElementById("deleteButton"),
        deleteMessage: document.getElementById("deleteMessage"),
        animalName: document.getElementById("animalName"),
        animalSpecies: document.getElementById("animalSpecies"),
        animalBreed: document.getElementById("animalBreed"),
        animalSex: document.getElementById("animalSex"),
        animalBirthDate: document.getElementById("animalBirthDate"),
        animalStatus: document.getElementById("animalStatus"),
        animalSterilized: document.getElementById("animalSterilized"),
        animalDescription: document.getElementById("animalDescription")
    };

    document.addEventListener("DOMContentLoaded", loadAnimalDetail);
    nodes.backButton.addEventListener("click", goBack);
    nodes.deleteButton.addEventListener("click", deleteAnimal);

    function findToken() {
        const keys = ["token", "authToken", "accessToken", "jwt"];
        for (const key of keys) {
            const value = localStorage.getItem(key) || sessionStorage.getItem(key);
            if (value) return value;
        }
        return null;
    }

    async function loadAnimalDetail() {
        if (!state.animalId) {
            showError("No se encontro el animal solicitado.");
            return;
        }

        if (!state.token) {
            showError("Inicia sesion para consultar el detalle.");
            return;
        }

        try {
            const response = await fetch(`/animals/${encodeURIComponent(state.animalId)}`, {
                headers: {
                    Authorization: `Bearer ${state.token}`
                }
            });

            if (!response.ok) {
                showError(await responseMessage(response));
                return;
            }

            const animal = await response.json();
            renderAnimal(animal);
        } catch (error) {
            showError("No fue posible conectar con el servidor.");
        }
    }

    function renderAnimal(animal) {
        document.title = `${animal.nombre} | Colitas Felices`;
        nodes.statusText.textContent = formatEnum(animal.estatus);
        nodes.statusText.classList.remove("error-state");
        nodes.animalName.textContent = animal.nombre;
        nodes.animalSpecies.textContent = animal.especie;
        nodes.animalBreed.textContent = animal.raza || "Sin raza registrada";
        nodes.animalSex.textContent = formatEnum(animal.sexo);
        nodes.animalBirthDate.textContent = formatDate(animal.fechaNacimiento);
        nodes.animalStatus.textContent = formatEnum(animal.estatus);
        nodes.animalSterilized.textContent = animal.esterilizado ? "Si" : "No";
        nodes.animalDescription.textContent = animal.descripcion;
        nodes.ownerBadge.hidden = !animal.esDueno;
        nodes.ownerActions.hidden = !animal.puedeEliminar;
    }

    async function deleteAnimal() {
        if (!window.confirm("Eliminar este animal?")) return;

        nodes.deleteButton.disabled = true;
        nodes.deleteMessage.textContent = "";

        try {
            const response = await fetch("/animals", {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${state.token}`
                },
                body: JSON.stringify({ animalId: state.animalId })
            });

            if (!response.ok) {
                nodes.deleteMessage.textContent = await responseMessage(response);
                return;
            }

            nodes.deleteMessage.textContent = "Animal eliminado correctamente.";
            nodes.deleteButton.hidden = true;
        } catch (error) {
            nodes.deleteMessage.textContent = "No fue posible conectar con el servidor.";
        } finally {
            nodes.deleteButton.disabled = false;
        }
    }

    function showError(message) {
        nodes.statusText.textContent = message;
        nodes.statusText.classList.add("error-state");
        nodes.ownerActions.hidden = true;
    }

    async function responseMessage(response) {
        const fallback = `Error ${response.status}`;
        const contentType = response.headers.get("content-type") || "";

        if (contentType.includes("application/json")) {
            const body = await response.json();
            if (body.error) return body.error;
            return Object.values(body).join(" ") || fallback;
        }

        const text = await response.text();
        return text || fallback;
    }

    function formatDate(value) {
        if (!value) return "-";
        return new Intl.DateTimeFormat("es-MX", {
            year: "numeric",
            month: "long",
            day: "numeric"
        }).format(new Date(`${value}T00:00:00`));
    }

    function formatEnum(value) {
        if (!value) return "-";
        return value
            .toLowerCase()
            .split("_")
            .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
            .join(" ");
    }

    function goBack() {
        if (window.history.length > 1) {
            window.history.back();
            return;
        }

        window.location.assign("/");
    }
})();
