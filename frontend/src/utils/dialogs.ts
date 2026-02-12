export function escapeHtml(value: string): string {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

export function renderSelectOptions(
  values: string[],
  placeholder: string,
  customLabel: string
): string {
  const options = values
    .map((v) => `<option value="${escapeHtml(v)}">${escapeHtml(v)}</option>`)
    .join("");
  return `
    <option value="">${escapeHtml(placeholder)}</option>
    ${options}
    <option value="__custom__">${escapeHtml(customLabel)}</option>
  `;
}

export function getApiErrorMessage(err: unknown, fallback: string): string {
  const ax = err as {
    serverMessage?: string;
    response?: { data?: unknown; status?: number };
  };
  if (ax.serverMessage) return ax.serverMessage;
  const data = ax.response?.data;
  if (data != null && typeof data === "string") return data;
  if (data != null && typeof data === "object") {
    const parts = [
      "message" in data && (data as { message?: unknown }).message,
      "error" in data && (data as { error?: unknown }).error
    ]
      .filter(Boolean)
      .map(String);
    if (parts.length) return parts.join(" — ");
  }
  if (ax.response?.status) return String(ax.response.status);
  return err instanceof Error ? err.message : fallback;
}

type SwalLike = { getPopup: () => HTMLElement | null; showValidationMessage: (msg: string) => void };

export function setupNewOrderCustomInputs(swal: SwalLike): void {
  const popup = swal.getPopup();
  if (!popup) return;
  const destinationSelect = popup.querySelector<HTMLSelectElement>("#order-destination");
  const cargoSelect = popup.querySelector<HTMLSelectElement>("#order-cargo");
  const destinationCustom = popup.querySelector<HTMLInputElement>("#order-destination-custom");
  const cargoCustom = popup.querySelector<HTMLInputElement>("#order-cargo-custom");

  const toggle = (
    select?: HTMLSelectElement | null,
    input?: HTMLInputElement | null
  ) => {
    if (!select || !input) return;
    const show = select.value === "__custom__";
    input.style.display = show ? "block" : "none";
    if (show) input.focus();
    else input.value = "";
  };

  toggle(destinationSelect, destinationCustom);
  toggle(cargoSelect, cargoCustom);
  destinationSelect?.addEventListener("change", () => toggle(destinationSelect, destinationCustom));
  cargoSelect?.addEventListener("change", () => toggle(cargoSelect, cargoCustom));
}

export function getNewOrderFormValues(
  popup: HTMLElement | null,
  validationMessage: string,
  swal: SwalLike
): { destination: string; cargoType: string; weight: number } | null {
  if (!popup) return null;
  const destSelect =
    popup.querySelector<HTMLSelectElement>("#order-destination")?.value?.trim() ?? "";
  const cargoSelect =
    popup.querySelector<HTMLSelectElement>("#order-cargo")?.value?.trim() ?? "";
  const destCustom =
    popup.querySelector<HTMLInputElement>("#order-destination-custom")?.value?.trim() ?? "";
  const cargoCustom =
    popup.querySelector<HTMLInputElement>("#order-cargo-custom")?.value?.trim() ?? "";
  const destination = destSelect === "__custom__" || !destSelect ? destCustom : destSelect;
  const cargoType = cargoSelect === "__custom__" || !cargoSelect ? cargoCustom : cargoSelect;
  const weightValue = popup.querySelector<HTMLInputElement>("#order-weight")?.value;
  const weight = weightValue ? Number(weightValue) : NaN;

  if (!destination || !cargoType || Number.isNaN(weight) || weight <= 0) {
    swal.showValidationMessage(validationMessage);
    return null;
  }
  return { destination, cargoType, weight };
}

export function getAddDriverFormValues(
  popup: HTMLElement | null,
  nowYear: number,
  validationMessage: string,
  swal: SwalLike
): { name: string; licenseYear: number; licenseCategories: string[] } | null {
  if (!popup) return null;
  const name = popup.querySelector<HTMLInputElement>("#driver-name")?.value?.trim() ?? "";
  const checked = popup.querySelectorAll<HTMLInputElement>('input[name="driver-cat"]:checked');
  const licenseCategories = checked ? Array.from(checked).map((el) => el.value) : [];
  const yearValue =
    popup.querySelector<HTMLSelectElement>("#driver-license-year")?.value?.trim() ?? "";
  const year = yearValue ? Number(yearValue) : NaN;

  if (!name || licenseCategories.length === 0 || Number.isNaN(year) || year < 1970 || year > nowYear) {
    swal.showValidationMessage(validationMessage);
    return null;
  }
  return { name, licenseYear: year, licenseCategories };
}

export function buildNewOrderHtml(
  destinationOptions: string[],
  cargoOptions: string[],
  t: (key: string) => string
): string {
  return (
    `<select id="order-destination" class="swal2-select">
      ${renderSelectOptions(destinationOptions, t("dialog.newOrder.selectDestination"), t("dialog.newOrder.customValue"))}
    </select>` +
    `<input id="order-destination-custom" class="swal2-input" placeholder="${escapeHtml(t("dialog.newOrder.customDestination"))}">` +
    `<select id="order-cargo" class="swal2-select">
      ${renderSelectOptions(cargoOptions, t("dialog.newOrder.selectCargo"), t("dialog.newOrder.customValue"))}
    </select>` +
    `<input id="order-cargo-custom" class="swal2-input" placeholder="${escapeHtml(t("dialog.newOrder.customCargo"))}">` +
    `<input id="order-weight" class="swal2-input" placeholder="${escapeHtml(t("dialog.newOrder.weight"))}" type="number" min="0" step="0.1">`
  );
}

export function buildAddDriverHtml(t: (key: string) => string, nowYear: number): string {
  const yearOptions = Array.from({ length: nowYear - 1969 }, (_, i) => String(1970 + i));
  const categories = ["A", "B", "C", "D", "E"];
  return (
    `<input id="driver-name" class="swal2-input" placeholder="${escapeHtml(t("dialog.addDriver.name"))}">` +
    `<div class="driver-categories" style="text-align:left; margin: 1rem 0;">
      <p style="margin:0 0 8px; font-size: 0.9em; color: var(--muted);">${escapeHtml(t("dialog.addDriver.licenseCategory"))}</p>
      ${categories.map((cat) => `<label style="display:inline-block; margin-right: 1rem;"><input type="checkbox" name="driver-cat" value="${cat}"> ${cat}</label>`).join("")}
    </div>` +
    `<select id="driver-license-year" class="swal2-select">
      <option value="">${escapeHtml(t("dialog.addDriver.selectYear"))}</option>
      ${yearOptions.map((y) => `<option value="${y}">${y}</option>`).join("")}
    </select>`
  );
}

export function buildEditDriverHtml(
  t: (key: string) => string,
  nowYear: number,
  driver: { driverName: string; licenseCategories?: string[]; licenseYear?: number }
): string {
  const yearOptions = Array.from({ length: nowYear - 1969 }, (_, i) => String(1970 + i));
  const categories = ["A", "B", "C", "D", "E"];
  const selectedCats = new Set((driver.licenseCategories ?? []).map((c) => c.toUpperCase()));
  const licenseYear = driver.licenseYear ?? nowYear;
  return (
    `<input id="driver-name" class="swal2-input" placeholder="${escapeHtml(t("dialog.addDriver.name"))}" value="${escapeHtml(driver.driverName)}">` +
    `<div class="driver-categories" style="text-align:left; margin: 1rem 0;">
      <p style="margin:0 0 8px; font-size: 0.9em; color: var(--muted);">${escapeHtml(t("dialog.addDriver.licenseCategory"))}</p>
      ${categories.map((cat) => `<label style="display:inline-block; margin-right: 1rem;"><input type="checkbox" name="driver-cat" value="${cat}" ${selectedCats.has(cat) ? "checked" : ""}> ${cat}</label>`).join("")}
    </div>` +
    `<select id="driver-license-year" class="swal2-select">
      <option value="">${escapeHtml(t("dialog.addDriver.selectYear"))}</option>
      ${yearOptions.map((y) => `<option value="${y}" ${y === String(licenseYear) ? "selected" : ""}>${y}</option>`).join("")}
    </select>`
  );
}
