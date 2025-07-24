# CatBreeds

This repository was created to document and version control a coding challenge proposed as part of a job application. The app demonstrates various Android development best practices and architectural patterns.

## Architecture Overview

The CatBreeds app follows Clean Architecture principles with a focus on data persistence and offline-first capabilities. The project uses modern Android development tools and libraries including:

- **Retrofit** for API communication
- **Room** for local data persistence
- **Kotlin Coroutines** for asynchronous operations
- **Flow** for reactive data streams

## Key Development Strategies

### 1. Online-First Strategy with Offline Fallback

The app implements an **online-first approach** where data is primarily fetched from The Cat API, with Room database serving as a backup when network connectivity is unavailable.

#### How it works:
- **Primary Data Source**: The app always attempts to fetch fresh data from the API first
- **Automatic Caching**: Successfully fetched data is automatically cached in the local Room database
- **Seamless Fallback**: When API calls fail (network issues, server downtime), the app seamlessly falls back to cached data
- **Favorite Preservation**: User favorites are preserved during cache updates using a REPLACE strategy that maintains the `isFavorite` status


### 2. Room Database Structure for Data Persistence

The app uses a well-structured Room database architecture designed for efficient data storage and retrieval:

#### Database Components:

**Entity Design (`CatBreedEntity`)**:
- Stores complete breed information including images
- Maintains favorite status locally
- Includes timestamp for cache management
- Uses `OnConflictStrategy.REPLACE` for smart updates

**DAO Operations (`CatBreedDao`)**:
- Pagination support for efficient memory usage
- Search functionality with SQL LIKE queries
- Favorite management with dedicated queries
- Flow-based reactive data streams for UI updates

**Key Features**:
- **Data Integrity**: Primary key constraints ensure no duplicate breeds
- **Efficient Queries**: Optimized SQL queries with proper indexing
- **Reactive Updates**: Flow-based data streams automatically update UI when data changes
- **Flexible Search**: Full-text search capabilities across breed names

### 3. API Communication Strategy

The app implements robust API communication with proper error handling and data transformation:

#### Network Layer Architecture:

**Service Interface (`CatApiService`)**:
- RESTful API endpoints for breed data
- Image fetching capabilities
- Search functionality
- Pagination support

**Network Configuration**:
- OkHttp client with logging interceptor for debugging
- Automatic API key injection via headers
- Proper content-type headers for JSON communication
- Error handling with graceful degradation

**Data Flow**:
1. **API Request** → Fetch breed data from The Cat API
2. **Image Enhancement** → Asynchronously fetch images for breeds with `reference_image_id`
3. **Data Transformation** → Convert API models to domain models
4. **Caching** → Store in Room database with favorite preservation
5. **UI Update** → Emit cached data through Flow to update UI

### 4. Data Consistency and User Experience

**Favorite Management**:
- Favorites are stored locally and survive app restarts
- Cache updates preserve user's favorite selections
- Toggle functionality works entirely offline

**Pagination**:
- Memory-efficient loading with configurable page sizes
- Smooth scrolling experience with cached data
- Consistent pagination between online and offline modes

**Search Functionality**:
- Real-time search with local database queries
- Search results include cached API data when available
- Fallback to local search when offline

## Benefits of This Architecture

1. **Reliability**: App works seamlessly offline with cached data
2. **Performance**: Fast data access through local database
3. **User Experience**: No data loss, preserved favorites, smooth navigation
4. **Scalability**: Easy to extend with new features and data sources
5. **Maintainability**: Clear separation of concerns with dependency injection

## Technical Highlights

- **Coroutine Usage**: Efficient async operations with proper error handling
- **Flow Integration**: Reactive UI updates without manual refresh calls
- **Result Pattern**: Consistent error handling throughout the app
- **Data Mapping**: Clean transformation between API, entity, and domain models

This architecture ensures a robust, user-friendly application that provides value even in challenging network conditions while maintaining data integrity and user preferences.
