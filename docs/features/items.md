# Items

## What it does

The service exposes item CRUD over HTTP. Each item has a server-assigned id, a name, and an
optional description.

| Method | Path | Success | Body |
| --- | --- | --- | --- |
| `GET` | `/items` | `200 OK` | list of items |
| `GET` | `/items/{id}` | `200 OK` | one item |
| `POST` | `/items` | `201 Created` | the created item |
| `PUT` | `/items/{id}` | `200 OK` | the replaced item |
| `DELETE` | `/items/{id}` | `204 No Content` | none |

The API document is generated from the controller and the DTOs by springdoc. `just spec`
regenerates `docs/openapi.json`.

## Data shape

An item, as returned by the API:

```json
{
  "id": 1,
  "name": "Widget",
  "description": "A small widget"
}
```

- `id` is a positive integer assigned by the database. It is absent from requests.
- `name` is required and at most 200 characters.
- `description` is optional and at most 2000 characters.

The domain type is `domain.Item`, the stored shape is `persistence.ItemEntity` in the `items`
table, and the transport shapes are `api.ItemRequest` and `api.ItemResponse`. The service creates
an item, then the store assigns the id.

## Failure modes

- An unknown id on `GET`, `PUT`, or `DELETE` returns `404 Not Found` as an RFC 7807 problem detail. The service raises `ItemNotFoundException` and `GlobalExceptionHandler` maps it.
- A `POST` or `PUT` body with a blank name, or a name over 200 characters, returns `400 Bad Request`. Bean validation runs at the controller boundary.
- A malformed JSON body returns `400 Bad Request`.
- The store is H2 in memory, so restarting the process discards every item.
