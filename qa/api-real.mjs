import assert from 'node:assert/strict';
import { createHmac } from 'node:crypto';
import { mkdir, writeFile } from 'node:fs/promises';
import path from 'node:path';

const base = process.env.API_URL ?? 'http://localhost:8080/api/v1';
const out = process.argv[2] ?? 'entrega/qa-api.json';
const cases = [];
const tokens = {};
async function request(name, method, route, expected, body, user = 'admin') {
  const headers = { 'Content-Type': 'application/json' };
  if (tokens[user]) headers.Authorization = `Bearer ${tokens[user]}`;
  const response = await fetch(base + route, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  const text = await response.text();
  let data;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  const record = { caso: name, metodo: method, ruta: route, esperado: expected, obtenido: response.status, correcto: response.status === expected };
  if (response.status >= 400) record.error = data;
  cases.push(record);
  assert.equal(response.status, expected, `${name}: ${response.status}, ${JSON.stringify(data)}`);
  if (expected >= 400) {
    for (const key of ['timestamp', 'status', 'error', 'mensaje', 'ruta']) assert.ok(data[key] !== undefined, `${name}: falta ${key}`);
    assert.equal(data.status, expected);
    for (const key of ['message', 'path', 'validationErrors']) assert.equal(data[key], undefined);
    if (expected !== 400) assert.equal(data.campos, undefined, `${name}: campos solo para validación`);
  }
  console.log(`${expected} ${name}`);
  return data;
}
function equalMoney(value, expected) { assert.equal(Math.round(value * 100), Math.round(expected * 100)); }
const order = (vehiculoId, services, kilometraje = 50000) => ({ vehiculoId, kilometraje, detalles: services.map(id => ({ servicioId: id, cantidad: 1 })) });
try {
  for (const user of ['admin', 'asesor', 'jefe']) {
    const login = await request(`Login ${user}`, 'POST', '/auth/login', 200, { username: user, password: 'Auto2026*' }, 'anonimo');
    assert.equal(login.tipo, 'Bearer'); assert.equal(login.expiraEnSegundos, 3600);
    assert.ok(Array.isArray(login.permisos)); assert.ok(Array.isArray(login.roles));
    tokens[user] = login.token;
  }
  const denied = [];
  for (const [username, password] of [['admin', 'incorrecta'], ['no-existe', 'Auto2026*'], ['inactivo', 'Auto2026*']]) {
    denied.push(await request(`Login rechazado ${username}`, 'POST', '/auth/login', 401, { username, password }, 'anonimo'));
  }
  assert.equal(new Set(denied.map(x => x.mensaje)).size, 1, 'Login no revela existencia o estado');
  for (const route of ['/clientes', '/servicios', '/vehiculos', '/ordenes', '/reportes/ingresos-por-servicio', '/auth/me']) {
    await request(`Sin token ${route}`, 'GET', route, 401, undefined, 'anonimo');
  }
  tokens.invalido = tokens.admin.slice(0, -8) + 'AAAAAAAA';
  await request('Firma alterada', 'GET', '/auth/me', 401, undefined, 'invalido');
  if (process.env.JWT_SECRET) {
    const [header, payload] = tokens.admin.split('.');
    const claims = JSON.parse(Buffer.from(payload, 'base64url').toString());
    claims.exp = Math.floor(Date.now() / 1000) - 120;
    claims.iat = claims.exp - 3600;
    const unsigned = `${header}.${Buffer.from(JSON.stringify(claims)).toString('base64url')}`;
    const alg = JSON.parse(Buffer.from(header, 'base64url').toString()).alg;
    const hash = { HS256: 'sha256', HS384: 'sha384', HS512: 'sha512' }[alg];
    assert.ok(hash, `Algoritmo JWT esperado: ${alg}`);
    tokens.vencido = `${unsigned}.${createHmac(hash, Buffer.from(process.env.JWT_SECRET, 'base64')).update(unsigned).digest('base64url')}`;
    await request('JWT vencido con firma válida', 'GET', '/auth/me', 401, undefined, 'vencido');
  }
  const me = await request('Identidad autenticada', 'GET', '/auth/me', 200);
  assert.equal(me.username, 'admin'); assert.equal(me.password, undefined);
  await request('ASESOR no crea servicios', 'POST', '/servicios', 403, {}, 'asesor');
  await request('ASESOR no consulta reportes', 'GET', '/reportes/ingresos-por-servicio', 403, undefined, 'asesor');
  await request('ASESOR no anula', 'PATCH', '/ordenes/1/anular', 403, { motivo: 'Prueba de permisos' }, 'asesor');
  await request('JEFE no registra órdenes', 'POST', '/ordenes', 403, {}, 'jefe');
  await request('JEFE no crea vehículos', 'POST', '/vehiculos', 403, {}, 'jefe');
  const clients = await request('Clientes reales', 'GET', '/clientes', 200);
  assert.ok(clients.length >= 6);
  let services = await request('Servicios reales', 'GET', '/servicios', 200);
  assert.ok(services.length >= 8);
  const active = services.filter(s => s.activo);
  const inactive = services.find(s => !s.activo);
  assert.ok(inactive);
  const vehicles = await request('Vehículos y cliente', 'GET', '/vehiculos', 200);
  assert.ok(vehicles.length >= 8); assert.ok(vehicles.every(v => v.clienteNombre));
  const filtered = await request('Filtro cliente', 'GET', `/vehiculos?clienteId=${clients[0].id}`, 200);
  assert.ok(filtered.length >= 2); assert.ok(filtered.every(v => v.clienteId === clients[0].id));
  const invalid = await request('Validación campos servicio', 'POST', '/servicios', 400, { codigo: 'MAL', nombre: 'x', precio: 0, duracionMin: 5, activo: true });
  assert.ok(invalid.campos?.codigo); assert.ok(invalid.campos?.precio);
  await request('Servicio duplicado', 'POST', '/servicios', 409, { codigo: services[0].codigo, nombre: 'Servicio duplicado QA', precio: 12, duracionMin: 30, activo: true });
  await request('Servicio inexistente', 'GET', '/servicios/999999999', 404);
  const code = Array.from({ length: 190 }, (_, i) => `SRV-${800 + i}`).find(c => !services.some(s => s.codigo === c));
  assert.ok(code);
  const serviceInput = { codigo: code, nombre: 'Servicio verificación API', precio: 33.25, duracionMin: 30, activo: true };
  const service = await request('Crear servicio', 'POST', '/servicios', 201, serviceInput);
  await request('Editar servicio conservando código propio', 'PUT', `/servicios/${service.id}`, 200, { ...serviceInput, nombre: 'Servicio API editado' });
  const disabled = await request('Baja de servicio', 'PATCH', `/servicios/${service.id}/estado`, 200, { activo: false });
  assert.equal(disabled.activo, false);
  await request('Eliminar servicio sin historial', 'DELETE', `/servicios/${service.id}`, 204);
  await request('Servicio eliminado inexistente', 'GET', `/servicios/${service.id}`, 404);
  await request('Servicio con historial protegido', 'DELETE', `/servicios/${active[0].id}`, 409);
  await request('Vehículo con historial protegido', 'DELETE', '/vehiculos/1', 409);
  await request('Placa duplicada', 'POST', '/vehiculos', 409, { placa: vehicles[0].placa, marca: 'Toyota', modelo: 'QA', anio: 2026, clienteId: clients[0].id });
  await request('Cliente inexistente', 'POST', '/vehiculos', 404, { placa: 'ZQA-998', marca: 'Toyota', modelo: 'QA', anio: 2026, clienteId: 999999999 });
  await request('Año y placa inválidos', 'POST', '/vehiculos', 400, { placa: 'INVALIDA', marca: 'Toyota', modelo: 'QA', anio: 1980, clienteId: clients[0].id });
  let plate;
  for (let i = 100; i < 999; i++) { const candidate = `QAT-${i}`; if (!vehicles.some(v => v.placa === candidate)) { plate = candidate; break; } }
  assert.ok(plate);
  const vehicleInput = { placa: plate, marca: 'Toyota', modelo: 'Prueba API', anio: 2026, clienteId: clients[0].id };
  const vehicle = await request('Crear vehículo', 'POST', '/vehiculos', 201, vehicleInput);
  await request('Editar vehículo conservando placa propia', 'PUT', `/vehiculos/${vehicle.id}`, 200, { ...vehicleInput, modelo: 'QA actualizado' });
  const before = await request('Órdenes antes del rechazo', 'GET', '/ordenes', 200);
  await request('Tercer detalle inactivo', 'POST', '/ordenes', 409, order(vehicle.id, [active[0].id, active[1].id, inactive.id]), 'asesor');
  const after = await request('Órdenes después del rechazo', 'GET', '/ordenes', 200);
  assert.equal(after.length, before.length, 'Rechazo no deja cabecera');
  await request('Servicios repetidos', 'POST', '/ordenes', 400, order(vehicle.id, [active[0].id, active[0].id]), 'asesor');
  await request('Cantidad mayor a cinco', 'POST', '/ordenes', 400, { vehiculoId: vehicle.id, kilometraje: 50000, detalles: [{ servicioId: active[0].id, cantidad: 6 }] }, 'asesor');
  await request('Orden vacía', 'POST', '/ordenes', 400, order(vehicle.id, []), 'asesor');
  await request('Once detalles', 'POST', '/ordenes', 400, order(vehicle.id, Array(11).fill(active[0].id)), 'asesor');
  await request('Servicio inexistente en orden', 'POST', '/ordenes', 404, order(vehicle.id, [999999999]), 'asesor');
  const km = await request('Kilometraje menor al histórico', 'POST', '/ordenes', 409, order(1, [active[0].id], 44999), 'asesor');
  assert.match(km.mensaje, /45000/);
  await request('Total enviado por cliente rechazado', 'POST', '/ordenes', 400, { ...order(vehicle.id, [active[0].id]), total: 0.01 }, 'asesor');
  const reportBefore = await request('Reporte agregado inicial', 'GET', '/reportes/ingresos-por-servicio', 200, undefined, 'jefe');
  const created = await request('Registrar orden completa', 'POST', '/ordenes', 201, { vehiculoId: vehicle.id, kilometraje: 50000, detalles: [{ servicioId: active[0].id, cantidad: 2 }, { servicioId: active[1].id, cantidad: 1 }] }, 'asesor');
  assert.match(created.numero, /^OS-\d{6}$/); assert.equal(created.usuarioRegistro, 'asesor'); assert.equal(created.estado, 'REGISTRADA');
  assert.equal(created.detalles.length, 2); equalMoney(created.total, active[0].precio * 2 + active[1].precio);
  const detail = await request('Consultar detalle', 'GET', `/ordenes/${created.id}`, 200);
  equalMoney(detail.total, created.total);
  for (const line of detail.detalles) equalMoney(line.subtotal, line.precioUnitario * line.cantidad);
  const reportRegistered = await request('Reporte suma orden registrada', 'GET', '/reportes/ingresos-por-servicio', 200);
  equalMoney(reportRegistered.reduce((n, x) => n + x.importeTotal, 0), reportBefore.reduce((n, x) => n + x.importeTotal, 0) + created.total);
  const found = await request('Filtros combinados órdenes', 'GET', `/ordenes?placa=${plate}&estado=REGISTRADA&desde=2026-01-01&hasta=2027-12-31`, 200);
  assert.ok(found.some(x => x.id === created.id)); assert.ok(found.every(x => x.placa === plate && x.estado === 'REGISTRADA'));
  await request('Fechas invertidas', 'GET', '/ordenes?desde=2026-10-30&hasta=2026-10-01', 400);
  await request('Formato fecha incorrecto', 'GET', '/ordenes?desde=no-fecha', 400);
  await request('Anulación fuera del día', 'PATCH', '/ordenes/1/anular', 409, { motivo: 'Prueba día anterior' }, 'jefe');
  await request('Motivo demasiado corto', 'PATCH', `/ordenes/${created.id}/anular`, 400, { motivo: 'no' }, 'jefe');
  const annulled = await request('Anular orden del día', 'PATCH', `/ordenes/${created.id}/anular`, 200, { motivo: 'Anulación de la verificación automática' }, 'jefe');
  assert.equal(annulled.estado, 'ANULADA');
  await request('Segunda anulación rechazada', 'PATCH', `/ordenes/${created.id}/anular`, 409, { motivo: 'Segundo intento de anulación' }, 'jefe');
  const reportAfter = await request('Reporte excluye anulada', 'GET', '/reportes/ingresos-por-servicio', 200);
  assert.deepEqual(reportAfter, reportBefore);
  await request('Reporte por rango sin resultados', 'GET', '/reportes/ingresos-por-servicio?desde=1990-01-01&hasta=1990-01-02', 200);
  await request('Historial anulado también protegido', 'DELETE', `/vehiculos/${vehicle.id}`, 409);
  cases.push({ caso: 'Rollback y totales comprobados mediante datos reales', correcto: true, ordenDePrueba: created.numero, estadoFinal: 'ANULADA', placaConHistorialDePrueba: plate });
} catch (error) {
  cases.push({ caso: 'Fallo de aserción', correcto: false, mensaje: error.message });
  console.error(error.message); process.exitCode = 1;
} finally {
  await mkdir(path.dirname(out), { recursive: true });
  const report = { fecha: new Date().toISOString(), api: base, total: cases.length, aprobadas: cases.filter(x => x.correcto).length, fallidas: cases.filter(x => !x.correcto).length, casos: cases };
  await writeFile(out, JSON.stringify(report, null, 2) + '\n');
  console.log(`Resultado: ${report.aprobadas}/${report.total}; ${out}`);
}
