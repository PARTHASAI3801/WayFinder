-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: routeplanner
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `rp_edges`
--

DROP TABLE IF EXISTS `rp_edges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rp_edges` (
  `id` int NOT NULL AUTO_INCREMENT,
  `from_loc` int NOT NULL,
  `to_loc` int NOT NULL,
  `distance` double NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `IDX_FROM_TO` (`from_loc`,`to_loc`),
  KEY `IDX_TO` (`to_loc`) /*!80000 INVISIBLE */,
  KEY `IDX_FROM` (`from_loc`),
  CONSTRAINT `FK_FROM_LOC` FOREIGN KEY (`from_loc`) REFERENCES `rp_locations` (`id`),
  CONSTRAINT `FK_TO_LOC` FOREIGN KEY (`to_loc`) REFERENCES `rp_locations` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16221 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rp_external_routes`
--

DROP TABLE IF EXISTS `rp_external_routes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rp_external_routes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `start_coords` longtext,
  `end_coords` longtext,
  `polyline` longtext NOT NULL,
  `mode` varchar(45) NOT NULL DEFAULT 'driving-car',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=647 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rp_external_via_route`
--

DROP TABLE IF EXISTS `rp_external_via_route`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rp_external_via_route` (
  `id` int NOT NULL AUTO_INCREMENT,
  `start_coords` mediumtext NOT NULL,
  `end_coords` mediumtext NOT NULL,
  `via_coords` longtext NOT NULL,
  `mode` varchar(255) NOT NULL DEFAULT 'driving-car',
  `created_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rp_locations`
--

DROP TABLE IF EXISTS `rp_locations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rp_locations` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=817 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rp_route_feedback`
--

DROP TABLE IF EXISTS `rp_route_feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rp_route_feedback` (
  `id` int NOT NULL AUTO_INCREMENT,
  `route_id` int NOT NULL,
  `route_type` varchar(255) DEFAULT NULL,
  `user_id` int NOT NULL,
  `rating` int NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_USER_ID_idx` (`user_id`),
  CONSTRAINT `FK_ROUTE_USER_ID` FOREIGN KEY (`user_id`) REFERENCES `rp_users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=94 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_route_feedback_ai` AFTER INSERT ON `rp_route_feedback` FOR EACH ROW INSERT INTO rp_user_activity_log (user_id, action, message, ref_table, ref_id, details)
VALUES (
  NEW.user_id,
  'FEEDBACK_ADDED',
  CONCAT('Added feedback (rating ', COALESCE(NEW.rating, 0), ') on ', NEW.route_type, ' route'),
  'rp_route_feedback',
  NEW.id,
  JSON_OBJECT('route_id', NEW.route_id, 'route_type', NEW.route_type, 'rating', NEW.rating, 'comment', NEW.comment)
) */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_route_feedback_au` AFTER UPDATE ON `rp_route_feedback` FOR EACH ROW INSERT INTO rp_user_activity_log (user_id, action, message, ref_table, ref_id, details)
VALUES (
  NEW.user_id,
  'FEEDBACK_UPDATED',
  CONCAT('Updated feedback (rating ', COALESCE(NEW.rating, 0), ') on ', NEW.route_type, ' route'),
  'rp_route_feedback',
  NEW.id,
  JSON_OBJECT('route_id', NEW.route_id, 'route_type', NEW.route_type, 'rating', NEW.rating, 'comment', NEW.comment)
) */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_route_feedback_ad` AFTER DELETE ON `rp_route_feedback` FOR EACH ROW INSERT INTO rp_user_activity_log (user_id, action, message, ref_table, ref_id, details)
VALUES (
  OLD.user_id,
  'FEEDBACK_DELETED',
  CONCAT('Deleted feedback (rating ', COALESCE(OLD.rating, 0), ') on ', OLD.route_type, ' route'),
  'rp_route_feedback',
  OLD.id,
  JSON_OBJECT('route_id', OLD.route_id, 'route_type', OLD.route_type, 'rating', OLD.rating, 'comment', OLD.comment)
) */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `rp_routes`
--

DROP TABLE IF EXISTS `rp_routes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rp_routes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `from_node_id` int DEFAULT NULL,
  `to_node_id` int DEFAULT NULL,
  `path_json` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_by` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `rating` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_from_route_idx` (`from_node_id`),
  KEY `fk_to_route_idx` (`to_node_id`),
  KEY `fk_created_by_idx` (`created_by`),
  CONSTRAINT `fk_created_by` FOREIGN KEY (`created_by`) REFERENCES `rp_users` (`id`),
  CONSTRAINT `fk_from_route` FOREIGN KEY (`from_node_id`) REFERENCES `rp_locations` (`id`),
  CONSTRAINT `fk_to_route` FOREIGN KEY (`to_node_id`) REFERENCES `rp_locations` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rp_user_activity_log`
--

DROP TABLE IF EXISTS `rp_user_activity_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rp_user_activity_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `action` varchar(64) NOT NULL,
  `message` varchar(1000) NOT NULL,
  `ref_table` varchar(64) NOT NULL,
  `ref_id` int DEFAULT NULL,
  `details` json DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`created_at`),
  KEY `idx_ref` (`ref_table`,`ref_id`)
) ENGINE=InnoDB AUTO_INCREMENT=127 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rp_user_contributed_routes`
--

DROP TABLE IF EXISTS `rp_user_contributed_routes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rp_user_contributed_routes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `route_type` varchar(255) NOT NULL DEFAULT 'INTERNAL',
  `start_loc_coords` longtext NOT NULL,
  `end_loc_coords` longtext NOT NULL,
  `coordinates_json` longtext NOT NULL,
  `description` varchar(1000) NOT NULL,
  `transport_mode` varchar(255) NOT NULL DEFAULT 'driving-car',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_before_insert_rp_user_contributed_routes` BEFORE INSERT ON `rp_user_contributed_routes` FOR EACH ROW BEGIN
  DECLARE coords_count INT;
  SET coords_count = JSON_LENGTH(NEW.coordinates_json);
  SET NEW.start_loc_coords = JSON_EXTRACT(NEW.coordinates_json, '$[0]');
  SET NEW.end_loc_coords = JSON_EXTRACT(NEW.coordinates_json, CONCAT('$[', coords_count - 1, ']'));
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_user_contrib_ai` AFTER INSERT ON `rp_user_contributed_routes` FOR EACH ROW INSERT INTO rp_user_activity_log (user_id, action, message, ref_table, ref_id, details)
VALUES (
  NEW.user_id,
  'CONTRIBUTED_ROUTE_CREATED',
  CONCAT('Contributed ', NEW.transport_mode, ' route: "', COALESCE(NEW.description, ''), '"'),
  'rp_user_contributed_routes',
  NEW.id,
  JSON_OBJECT('route_type', NEW.route_type, 'transport_mode', NEW.transport_mode, 'description', NEW.description)
) */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_before_update_rp_user_contributed_routes` BEFORE UPDATE ON `rp_user_contributed_routes` FOR EACH ROW BEGIN
  DECLARE coords_count INT;

  SET coords_count = JSON_LENGTH(NEW.coordinates_json);

  SET NEW.start_loc_coords = JSON_EXTRACT(NEW.coordinates_json, '$[0]');
  SET NEW.end_loc_coords = JSON_EXTRACT(NEW.coordinates_json, CONCAT('$[', coords_count - 1, ']'));
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_user_contrib_au` AFTER UPDATE ON `rp_user_contributed_routes` FOR EACH ROW INSERT INTO rp_user_activity_log (user_id, action, message, ref_table, ref_id, details)
VALUES (
  NEW.user_id,
  'CONTRIBUTED_ROUTE_UPDATED',
  CONCAT('Updated contributed ', NEW.transport_mode, ' route: "', COALESCE(NEW.description, ''), '"'),
  'rp_user_contributed_routes',
  NEW.id,
  JSON_OBJECT('route_type', NEW.route_type, 'transport_mode', NEW.transport_mode, 'description', NEW.description)
) */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_user_contrib_ad` AFTER DELETE ON `rp_user_contributed_routes` FOR EACH ROW INSERT INTO rp_user_activity_log (user_id, action, message, ref_table, ref_id, details)
VALUES (
  OLD.user_id,
  'CONTRIBUTED_ROUTE_DELETED',
  CONCAT('Deleted contributed ', OLD.transport_mode, ' route: "', COALESCE(OLD.description, ''), '"'),
  'rp_user_contributed_routes',
  OLD.id,
  JSON_OBJECT('route_type', OLD.route_type, 'transport_mode', OLD.transport_mode, 'description', OLD.description)
) */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `rp_user_mgmt`
--

DROP TABLE IF EXISTS `rp_user_mgmt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rp_user_mgmt` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `ph_number` varchar(255) DEFAULT NULL,
  `bio` varchar(255) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_USER_ID_idx` (`user_id`),
  CONSTRAINT `FK_USER_ID` FOREIGN KEY (`user_id`) REFERENCES `rp_users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rp_user_saved_routes`
--

DROP TABLE IF EXISTS `rp_user_saved_routes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rp_user_saved_routes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `route_id` int NOT NULL,
  `route_type` varchar(255) NOT NULL DEFAULT 'EXTERNAL',
  `name` varchar(255) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `saved_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_USER_ID1_idx` (`user_id`),
  CONSTRAINT `FK_USER_ID1` FOREIGN KEY (`user_id`) REFERENCES `rp_users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_saved_routes_ai` AFTER INSERT ON `rp_user_saved_routes` FOR EACH ROW INSERT INTO rp_user_activity_log (user_id, action, message, ref_table, ref_id, details)
VALUES (
  NEW.user_id,
  'SAVED_ROUTE_CREATED',
  CONCAT('Saved ', NEW.route_type, ' route: "', COALESCE(NEW.name, ''), '"'),
  'rp_user_saved_routes',
  NEW.id,
  JSON_OBJECT('route_type', NEW.route_type, 'name', NEW.name)
) */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_saved_routes_au` AFTER UPDATE ON `rp_user_saved_routes` FOR EACH ROW INSERT INTO rp_user_activity_log (user_id, action, message, ref_table, ref_id, details)
VALUES (
  NEW.user_id,
  'SAVED_ROUTE_UPDATED',
  CONCAT('Updated saved ', NEW.route_type, ' route: "', COALESCE(NEW.name, ''), '"'),
  'rp_user_saved_routes',
  NEW.id,
  JSON_OBJECT('route_type', NEW.route_type, 'name', NEW.name, 'route_id', NEW.route_id)
) */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_saved_routes_ad` AFTER DELETE ON `rp_user_saved_routes` FOR EACH ROW INSERT INTO rp_user_activity_log (user_id, action, message, ref_table, ref_id, details)
VALUES (
  OLD.user_id,
  'SAVED_ROUTE_DELETED',
  CONCAT('Deleted saved ', OLD.route_type, ' route: "', COALESCE(OLD.name, ''), '"'),
  'rp_user_saved_routes',
  OLD.id,
  JSON_OBJECT('route_type', OLD.route_type, 'name', OLD.name, 'route_id', OLD.route_id)
) */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `rp_users`
--

DROP TABLE IF EXISTS `rp_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rp_users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `uname` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `last_login` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `full_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary view structure for view `v_user_activity_feed`
--

DROP TABLE IF EXISTS `v_user_activity_feed`;
/*!50001 DROP VIEW IF EXISTS `v_user_activity_feed`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_user_activity_feed` AS SELECT 
 1 AS `id`,
 1 AS `userid`,
 1 AS `title`,
 1 AS `action`,
 1 AS `metadata`,
 1 AS `time`*/;
SET character_set_client = @saved_cs_client;

--
-- Dumping events for database 'routeplanner'
--

--
-- Dumping routines for database 'routeplanner'
--

--
-- Final view structure for view `v_user_activity_feed`
--

/*!50001 DROP VIEW IF EXISTS `v_user_activity_feed`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_user_activity_feed` AS select `l`.`id` AS `id`,`l`.`user_id` AS `userid`,`l`.`message` AS `title`,`l`.`action` AS `action`,json_merge_patch(coalesce(`l`.`details`,json_object()),json_object('refTable',`l`.`ref_table`,'refId',`l`.`ref_id`)) AS `metadata`,`l`.`created_at` AS `time` from `rp_user_activity_log` `l` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-05  0:43:18
