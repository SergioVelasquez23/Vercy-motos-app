# Documentación de la API

## Endpoints principales

### Usuarios

#### Crear un usuario

- **URL**: `/api/users`
- **Método**: `POST`
- **Descripción**: Crea un nuevo usuario.
- **Cuerpo de la solicitud**:
  ```json
  {
    "username": "string",
    "password": "string",
    "email": "string"
  }
  ```
- **Respuesta**:
  ```json
  {
    "id": "string",
    "username": "string",
    "email": "string"
  }
  ```

#### Obtener todos los usuarios

- **URL**: `/api/users`
- **Método**: `GET`
- **Descripción**: Devuelve una lista de todos los usuarios.
- **Respuesta**:
  ```json
  [
    {
      "id": "string",
      "username": "string",
      "email": "string"
    }
  ]
  ```

### Roles

#### Crear un rol

- **URL**: `/api/roles`
- **Método**: `POST`
- **Descripción**: Crea un nuevo rol.
- **Cuerpo de la solicitud**:
  ```json
  {
    "name": "string",
    "description": "string"
  }
  ```
- **Respuesta**:
  ```json
  {
    "id": "string",
    "name": "string",
    "description": "string"
  }
  ```

#### Obtener todos los roles

- **URL**: `/api/roles`
- **Método**: `GET`
- **Descripción**: Devuelve una lista de todos los roles.
- **Respuesta**:
  ```json
  [
    {
      "id": "string",
      "name": "string",
      "description": "string"
    }
  ]
  ```

### Permisos

#### Crear un permiso

- **URL**: `/api/permissions`
- **Método**: `POST`
- **Descripción**: Crea un nuevo permiso.
- **Cuerpo de la solicitud**:
  ```json
  {
    "name": "string",
    "description": "string"
  }
  ```
- **Respuesta**:
  ```json
  {
    "id": "string",
    "name": "string",
    "description": "string"
  }
  ```

#### Obtener todos los permisos

- **URL**: `/api/permissions`
- **Método**: `GET`
- **Descripción**: Devuelve una lista de todos los permisos.
- **Respuesta**:
  ```json
  [
    {
      "id": "string",
      "name": "string",
      "description": "string"
    }
  ]
  ```

## Notas

- Todos los endpoints requieren autenticación mediante JWT.
- Consulta la documentación de autenticación para obtener más detalles sobre cómo generar y usar tokens JWT.
